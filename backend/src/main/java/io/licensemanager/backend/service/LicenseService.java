package io.licensemanager.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.LicenseTemplate;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.CustomerRepository;
import io.licensemanager.backend.repository.LicenseRepository;
import io.licensemanager.backend.repository.LicenseTemplateRepository;
import io.licensemanager.backend.repository.UserRepository;
import io.licensemanager.backend.util.CryptoUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

}