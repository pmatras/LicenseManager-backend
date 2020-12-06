package io.licensemanager.backend.service;

import io.licensemanager.backend.configuration.setup.SUPPORTED_FIELD_TYPES;
import io.licensemanager.backend.entity.LicenseTemplate;
import io.licensemanager.backend.entity.User;
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
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseTemplateService.class);

    private final LicenseTemplateRepository licenseTemplateRepository;
    private final UserRepository userRepository;
    private final LicenseRepository licenseRepository;

    public List<LicenseTemplate> getLicenseTemplatesList() {
        return licenseTemplateRepository.findAll();
    }

    public List<String> getSupportedFieldTypes() {
        return Stream.of(SUPPORTED_FIELD_TYPES.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<LicenseTemplate> createLicenseTemplate(final String name, final Map<String, Class> fields,
                                                           final String creatorsUsername) {
        logger.debug("Creating new license template with name {}", name);
        Optional<LicenseTemplate> existingTemplate = licenseTemplateRepository.findByName(name);
        if (existingTemplate.isEmpty()) {
            Optional<User> creator = userRepository.findByUsername(creatorsUsername);
            if (creator.isEmpty()) {
                logger.error("Cannot create requested license template - user with passed username doesn't exist");
                return Optional.empty();
            }
            LicenseTemplate template = new LicenseTemplate();
            template.setName(name);
            template.setFields(fields);
            template.setCreationTime(LocalDateTime.now());
            template.setCreator(creator.get());
            Optional<KeyPair> keyPair = CryptoUtils.generateKeyPair();
            if (keyPair.isPresent()) {
                template.setPrivateKey(keyPair.get().getPrivate());
                template.setPublicKey(keyPair.get().getPublic());
            }

            return Optional.of(licenseTemplateRepository.save(template));
        }
        logger.error("License with requested name already exists, creation skipped");

        return Optional.empty();
    }

    @Transactional
    public boolean deleteLicenseTemplate(final Long templateId) {
        logger.debug("Deleting license template with id {}", templateId);
        Optional<LicenseTemplate> templateToDelete = licenseTemplateRepository.findById(templateId);
        if (templateToDelete.isPresent()) {
            if (licenseRepository.existsByUsedTemplateIs(templateToDelete.get())) {
                logger.error("Cannot delete license template with id {} - some license(s) uses it", templateId);

                return false;
            }
            licenseTemplateRepository.delete(templateToDelete.get());

            return true;
        }
        logger.error("Requested license template doesn't exist");

        return false;
    }

    @Transactional
    public byte[] getTemplatePublicKey(final Long templateId) {
        logger.debug("Getting public key of license template");
        Optional<LicenseTemplate> template = licenseTemplateRepository.findById(templateId);

        if(template.isPresent()) {
            return template.get().getPublicKey().getEncoded();
        }
        logger.error("Requested license template doesn't exist");

        return new byte[0];
    }

}
