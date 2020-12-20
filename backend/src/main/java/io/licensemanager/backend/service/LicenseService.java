package io.licensemanager.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.licensemanager.backend.configuration.setup.Operation;
import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.*;
import io.licensemanager.backend.event.publisher.LicenseGenerationEventPublisher;
import io.licensemanager.backend.event.publisher.SystemOperationEventPublisher;
import io.licensemanager.backend.model.FileDetails;
import io.licensemanager.backend.model.LicensesStatus;
import io.licensemanager.backend.model.response.CustomersLicensesStatistics;
import io.licensemanager.backend.model.response.LicenseFileContentResponse;
import io.licensemanager.backend.model.response.LicensesStatistics;
import io.licensemanager.backend.repository.CustomerRepository;
import io.licensemanager.backend.repository.LicenseRepository;
import io.licensemanager.backend.repository.LicenseTemplateRepository;
import io.licensemanager.backend.repository.UserRepository;
import io.licensemanager.backend.util.CryptoUtils;
import io.licensemanager.backend.util.LicensesStatisticsGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    private final LicenseRepository licenseRepository;
    private final LicenseTemplateRepository licenseTemplateRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final LicenseGenerationEventPublisher eventPublisher;
    private final SystemOperationEventPublisher systemOperationEventPublisher;

    @Transactional
    public List<License> getLicensesList(final String username, final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Getting licenses list");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user which requested customers list");
            return Collections.emptyList();
        }
        User creator = user.get();
        if (permissions.contains(ROLES_PERMISSIONS.VIEW_ALL_LICENSES)
                || permissions.contains(ROLES_PERMISSIONS.ALL)) {
            return licenseRepository.findAll();
        }

        return licenseRepository.findAllByCreatorIs(creator);
    }

    @Transactional
    public LicenseFileContentResponse getDecryptedLicenseFileContent(final Long licenseId, final String username,
                                                                     final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Getting encrypted license file's content");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user which requested license file content");
            return new LicenseFileContentResponse(false, "");
        }
        User creator = user.get();
        Optional<License> license;
        if (permissions.contains(ROLES_PERMISSIONS.VIEW_ALL_CUSTOMERS)
                || permissions.contains(ROLES_PERMISSIONS.ALL)) {
            license = licenseRepository.findById(licenseId);
        } else {
            license = licenseRepository.findByIdAndCreatorIs(licenseId, creator);
        }

        if (license.isPresent()) {
            License licenseToDecrypt = license.get();
            byte[] encrypted = licenseToDecrypt.getLicenseFile();

            PublicKey publicKey = licenseToDecrypt.getUsedTemplate().getPublicKey();
            return new LicenseFileContentResponse(
                    CryptoUtils.verifySign(encrypted, licenseToDecrypt.getLicenseKey(), publicKey),
                    CryptoUtils.decrypt(encrypted, publicKey)
            );

        }
        logger.error("Requested license doesn't exist");

        return new LicenseFileContentResponse(false, "");
    }

    @Transactional
    public Optional<License> generateLicense(final String name, final String expirationDateString,
                                             final Map<String, Object> values,
                                             final Long templateId, final Long customerId,
                                             final String creatorsUsername) {
        logger.info("Generating new license with name {}", name);
        Optional<User> creator = userRepository.findByUsername(creatorsUsername);
        if (creator.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return Optional.empty();
        }
        Optional<LicenseTemplate> templateOptional = licenseTemplateRepository.findById(templateId);
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (templateOptional.isEmpty() || customerOptional.isEmpty()) {
            logger.error("License template or customer doesn't exist - cannot generate license");
            return Optional.empty();
        }

        LicenseTemplate template = templateOptional.get();
        Customer customer = customerOptional.get();

        License license = new License();
        license.setName(name);
        LocalDateTime currentDate = LocalDateTime.now();
        license.setLicenseFileName(
                String.format("%s_%s.license", customer.getName(), currentDate.toLocalDate().toString())
                        .replaceAll("\\s", "_")
        );
        license.setGenerationDate(currentDate);
        LocalDateTime expirationDate = currentDate.plusMonths(1L);
        try {
            expirationDate = LocalDateTime.parse(expirationDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            logger.error("Failed to parse requested expiration date, reason - {}, default one month will be used", e.getMessage());
        }
        license.setExpirationDate(expirationDate);
        license.setCustomer(customer);
        license.setUsedTemplate(template);
        license.setCreator(creator.get());
        license.setIsExpired(!currentDate.isBefore(expirationDate));
        license.setIsActive(true);

        String licenseFileContent = parseLicenseToJson(
                template.getFields(),
                values,
                customer.getName(),
                currentDate,
                expirationDate
        );

        byte[] encryptedContent = CryptoUtils.encrypt(licenseFileContent, template.getPublicKey());
        String licenseKey = CryptoUtils.signContent(encryptedContent, template.getPrivateKey());
        license.setLicenseKey(licenseKey);
        license.setLicenseFile(encryptedContent);

        boolean isAdmin = creator.get().getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch("ADMIN"::equals);
        if (!isAdmin) {
            eventPublisher.publishEvent(creator.get(), license);
        }
        systemOperationEventPublisher.publishEvent(creatorsUsername, Operation.LICENSE_GENERATION,
                String.format("License %s for %s generated", license.getName(), license.getCustomer().getName())
        );

        return Optional.of(licenseRepository.save(license));
    }

    private String parseLicenseToJson(final Map<String, Class> fields, Map<String, Object> values,
                                      final String customerName, final LocalDateTime generationDate,
                                      final LocalDateTime expirationDate) {
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectNode jsonObject = jsonMapper.createObjectNode();

        jsonObject.put("customerName", customerName);
        jsonObject.put("creationDate", generationDate.toString());
        jsonObject.put("expirationDate", expirationDate.toString());

        fields.forEach((fieldName, fieldType) -> {
            switch (fieldType.getSimpleName()) {
                case "String":
                    jsonObject.put(fieldName, (String) values.get(fieldName));
                    break;
                case "Integer":
                    jsonObject.put(fieldName, (Integer) values.get(fieldName));
                    break;
                case "Long":
                    jsonObject.put(fieldName, (Long) values.get(fieldName));
                    break;
                case "Float":
                    jsonObject.put(fieldName, (Float) values.get(fieldName));
                    break;
                case "Double":
                    jsonObject.put(fieldName, (Double) values.get(fieldName));
                    break;
                case "Boolean":
                    jsonObject.put(fieldName, (Boolean) values.get(fieldName));
                    break;
                case "Character":
                    jsonObject.put(fieldName, (Character) values.get(fieldName));
                    break;
            }
        });

        String json = "{}";
        try {
            json = jsonMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse license's content as json, reason - {}", e.getMessage());
        }

        return json;
    }

    private boolean changeActiveStatus(final Long licenseId, final String username,
                                       final Set<ROLES_PERMISSIONS> permissions, final Boolean value) {
        logger.debug("Changing active status of license with id {} to {}", licenseId, value);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return false;
        }

        Optional<License> license;
        if (permissions.contains(ROLES_PERMISSIONS.ALL) || permissions.contains(ROLES_PERMISSIONS.DELETE_ALL_LICENSES)) {
            license = licenseRepository.findById(licenseId);
        } else {
            license = licenseRepository.findByIdAndCreatorIs(licenseId, user.get());
        }

        if (license.isPresent()) {
            License licenseToChange = license.get();
            licenseToChange.setIsActive(value);
            licenseRepository.save(licenseToChange);

            return true;
        }

        return false;
    }

    public boolean disableLicense(final Long licenseId, final String username, final Set<ROLES_PERMISSIONS> permissions) {
        return changeActiveStatus(licenseId, username, permissions, false);
    }

    public boolean reactivateLicense(final Long licenseId, final String username, final Set<ROLES_PERMISSIONS> permissions) {
        return changeActiveStatus(licenseId, username, permissions, true);
    }

    private String changeJsonStringFieldValue(final String jsonString, final String field, final String value) {
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            JsonNode jsonObject = jsonMapper.readTree(jsonString);
            ObjectNode json = (ObjectNode) jsonObject;
            json.set(field, TextNode.valueOf(value));

            return json.toString();
        } catch (JsonProcessingException e) {
            logger.error("Cannot change string field value, reason - {}", e.getMessage());
        }

        return jsonString;
    }

    public boolean extendLicenseExpirationDate(final Long licenseId, final String expirationDate,
                                               final String username, final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Changing license's expiration date");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return false;
        }

        Optional<License> license;
        if (permissions.contains(ROLES_PERMISSIONS.ALL) || permissions.contains(ROLES_PERMISSIONS.EDIT_ALL_LICENSES)) {
            license = licenseRepository.findById(licenseId);
        } else {
            license = licenseRepository.findByIdAndCreatorIs(licenseId, user.get());
        }

        if (license.isPresent()) {
            License licenseToExtend = license.get();
            LocalDateTime currentExpirationDate = licenseToExtend.getExpirationDate();
            LocalDateTime extendedExpirationDate = null;
            try {
                extendedExpirationDate = LocalDateTime.parse(expirationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse requested expiration date, reason - {}, license's expiration date won't be extended",
                        e.getMessage()
                );
            }
            if (extendedExpirationDate != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                licenseToExtend.setExpirationDate(extendedExpirationDate);
                licenseToExtend.setIsExpired(!currentDate.isBefore(extendedExpirationDate));

                PublicKey publicKey = licenseToExtend.getUsedTemplate().getPublicKey();
                PrivateKey privateKey = licenseToExtend.getUsedTemplate().getPrivateKey();
                byte[] encryptedFile = licenseToExtend.getLicenseFile();
                String decryptedFile = CryptoUtils.decrypt(encryptedFile, publicKey);
                String updatedFile = changeJsonStringFieldValue(decryptedFile, "expirationDate", extendedExpirationDate.toString());
                byte[] updatedEncrypted = CryptoUtils.encrypt(updatedFile, publicKey);
                String updatedLicenseKey = CryptoUtils.signContent(updatedEncrypted, privateKey);
                licenseToExtend.setLicenseFile(updatedEncrypted);
                licenseToExtend.setLicenseKey(updatedLicenseKey);
                licenseRepository.save(licenseToExtend);

                systemOperationEventPublisher.publishEvent(username, Operation.LICENSE_EDITION,
                        String.format("License's %s for %s expiration date changed from %s to %s", licenseToExtend.getName(),
                                licenseToExtend.getCustomer().getName(), currentExpirationDate, extendedExpirationDate)
                );

                return true;
            }
        }
        logger.error("Failed to change license's expiration date - requested license doesn't exist");

        return false;

    }

    @Transactional
    public FileDetails getLicenseFile(final Long licenseId, final String username,
                                      final Set<ROLES_PERMISSIONS> permissions) {
        logger.info("Getting license file");
        FileDetails file = new FileDetails();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return file;
        }

        Optional<License> license;
        if (permissions.contains(ROLES_PERMISSIONS.ALL) || permissions.contains(ROLES_PERMISSIONS.VIEW_ALL_LICENSES)) {
            license = licenseRepository.findById(licenseId);
        } else {
            license = licenseRepository.findByIdAndCreatorIs(licenseId, user.get());
        }

        if (license.isPresent()) {
            License licenseToDownload = license.get();
            file.setFileName(licenseToDownload.getLicenseFileName());
            file.setContentLength(licenseToDownload.getLicenseFile().length);
            file.setContent(licenseToDownload.getLicenseFile());

            systemOperationEventPublisher.publishEvent(username, Operation.LICENSE_DOWNLOAD,
                    String.format("License's %s file for %s downloaded", licenseToDownload.getName(),
                            licenseToDownload.getCustomer().getName())
            );
            return file;
        }
        logger.error("License with requested id doesn't exist");

        return file;
    }

    @Transactional
    public FileDetails getLicenseFileKeys(final Long licenseId, final String username, final Set<ROLES_PERMISSIONS> permissions) {
        logger.info("Getting license file keys");
        FileDetails file = new FileDetails();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return file;
        }

        Optional<License> license;
        if (permissions.contains(ROLES_PERMISSIONS.ALL) || permissions.contains(ROLES_PERMISSIONS.VIEW_ALL_LICENSES)) {
            license = licenseRepository.findById(licenseId);
        } else {
            license = licenseRepository.findByIdAndCreatorIs(licenseId, user.get());
        }

        if (license.isPresent()) {
            License licenseToDownload = license.get();
            file.setFileName(String.format("%skeys", licenseToDownload.getLicenseFileName()));
            StringBuilder licenseKeys = new StringBuilder();
            licenseKeys
                    .append("public RSA key:\n")
                    .append(Base64.getEncoder().encodeToString(licenseToDownload.getUsedTemplate().getPublicKey().getEncoded()))
                    .append("\n\nlicense key:\n")
                    .append(licenseToDownload.getLicenseKey());
            byte[] encoded = licenseKeys.toString().getBytes(StandardCharsets.UTF_8);
            file.setContentLength(encoded.length);
            file.setContent(encoded);

            return file;
        }
        logger.error("License with requested id doesn't exist");

        return file;

    }

    @Transactional
    public LicensesStatistics getLicensesStats(final String username, final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Getting licenses statistics");

        return LicensesStatisticsGenerator.generateStats(getLicensesList(username, permissions));
    }

    private List<License> getLicensesListForCustomer(final Long customerId, final String username,
                                                     final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Getting licenses list for customer");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user who requested licenses list for customer");
            return Collections.emptyList();
        }
        User creator = user.get();
        if (permissions.contains(ROLES_PERMISSIONS.VIEW_ALL_LICENSES)
                || permissions.contains(ROLES_PERMISSIONS.ALL)) {
            return licenseRepository.findAllByCustomerId(customerId);
        }

        return licenseRepository.findAllByCustomerIdAndCreatorIs(customerId, creator);

    }

    public CustomersLicensesStatistics getCustomersLicensesStats(final Long customerId, final String username,
                                                                 final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Getting licenses statistics for customer");

        return LicensesStatisticsGenerator.generateStatsForCustomer(getLicensesListForCustomer(customerId,
                username, permissions));
    }

    @Transactional
    public LicensesStatus checkLicensesValidity() {
        logger.info("Checking licenses validity started");
        LicensesStatus licensesStatus = new LicensesStatus();
        List<License> licenses = licenseRepository.findAll();
        LocalDateTime currentTime = LocalDateTime.now();

        licenses.forEach(license -> {
            LocalDateTime expirationDate = license.getExpirationDate();
            if (currentTime.isBefore(expirationDate)) {
                licensesStatus.incrementValidLicensesCount();
                license.setIsExpired(false);
            } else {
                licensesStatus.incrementExpiredLicensesCount();
                license.setIsExpired(true);
            }
        });

        licenseRepository.saveAll(licenses);
        logger.info("Checking licenses validity finished");

        return licensesStatus;
    }

}
