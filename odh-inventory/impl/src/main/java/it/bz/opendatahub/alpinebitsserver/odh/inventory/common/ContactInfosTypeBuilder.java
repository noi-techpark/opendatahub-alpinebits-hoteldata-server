// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.common;

import it.bz.opendatahub.alpinebits.xml.schema.ota.AddressesType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.AddressesType.Address;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ContactInfoRootType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ContactInfosType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.CountryNameType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.EmailsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.EmailsType.Email;
import it.bz.opendatahub.alpinebits.xml.schema.ota.PhonesType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.PhonesType.Phone;
import it.bz.opendatahub.alpinebits.xml.schema.ota.URLsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.URLsType.URL;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.AccoDetail;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.Accommodation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Helper class to build {@link ContactInfosType} instances.
 */
public final class ContactInfosTypeBuilder {

    private ContactInfosTypeBuilder() {
        // Empty
    }

    /**
     * Try to extract a {@link ContactInfosType} from the given {@link Accommodation}.
     *
     * @param accommodation The accommodation data is used to extract a ContactInfosType instance.
     * @return An {@link Optional} wrapping the ContactInfosType if such an instance could be
     * extracted from the given accommodation, {@link Optional#empty()} otherwise.
     */
    public static Optional<ContactInfosType> extractContactInfosType(Accommodation accommodation) {
        if (accommodation == null) {
            return Optional.empty();
        }

        ContactInfoRootType contactInfoType = new ContactInfoRootType();
        contactInfoType.setLocation("6");

        extractAddressesType(accommodation).ifPresent(contactInfoType::setAddresses);
        extractPhonesType(accommodation).ifPresent(contactInfoType::setPhones);
        extractEmailsType(accommodation).ifPresent(contactInfoType::setEmails);
        extractURLsType(accommodation).ifPresent(contactInfoType::setURLs);

        // Check if any data could be extracted from Accommodation
        // If none was extracted, then the whole ContactInfosType should be empty
        if (isContactInfoRootTypeEmpty(contactInfoType)) {
            return Optional.empty();
        }

        // If there is extracted data, return the wrapped the ContactInfoRootType
        ContactInfosType contactInfosType = new ContactInfosType();
        contactInfosType.getContactInfos().add(contactInfoType);

        return Optional.of(contactInfosType);
    }

    private static Optional<AddressesType> extractAddressesType(Accommodation accommodation) {
        if (accommodation.getAccoDetailMap() == null || accommodation.getAccoDetailMap().isEmpty()) {
            return Optional.empty();
        }

        List<Address> addresses = accommodation.getAccoDetailMap().entrySet().stream()
                .map(entry -> {
                    String language = entry.getKey();
                    AccoDetail accoDetail = entry.getValue();

                    Address address = new Address();
                    address.setLanguage(language);

                    if (isStringNonEmpty(accoDetail.getStreet())) {
                        address.getAddressLines().add(accoDetail.getStreet());
                    }
                    if (isStringNonEmpty(accoDetail.getCity())) {
                        address.setCityName(accoDetail.getCity());
                    }
                    if (isStringNonEmpty(accoDetail.getZip())) {
                        address.setPostalCode(accoDetail.getZip());
                    }
                    if (isStringNonEmpty(accoDetail.getCountryCode())) {
                        CountryNameType countryNameType = new CountryNameType();
                        countryNameType.setCode(accoDetail.getCountryCode());
                        address.setCountryName(countryNameType);
                    }

                    return isAddressEmpty(address) ? null : address;
                })
                .filter(Objects::nonNull)
                .toList();

        // If there are no addresses, return an empty Optional
        if (addresses.isEmpty()) {
            return Optional.empty();
        }

        AddressesType addressesType = new AddressesType();
        addressesType.getAddresses().addAll(addresses);

        return Optional.of(addressesType);
    }

    private static boolean isAddressEmpty(Address address) {
        return address.getAddressLines() == null && address.getCityName() == null && address.getPostalCode() == null;
    }

    private static Optional<PhonesType> extractPhonesType(Accommodation accommodation) {
        if (accommodation.getAccoDetailMap() == null || accommodation.getAccoDetailMap().isEmpty()) {
            return Optional.empty();
        }

        Set<String> phoneNumbers = new HashSet<>();
        Set<String> mobileNumbers = new HashSet<>();

        for (AccoDetail accoDetail : accommodation.getAccoDetailMap().values()) {
            if (isStringNonEmpty(accoDetail.getPhone())) {
                phoneNumbers.add(accoDetail.getPhone());
            }
            if (isStringNonEmpty(accoDetail.getMobile())) {
                mobileNumbers.add(accoDetail.getMobile());
            }
        }

        // If there are no phone an mobile numbers, return an empty Optional
        if (phoneNumbers.isEmpty() && mobileNumbers.isEmpty()) {
            return Optional.empty();
        }

        List<Phone> phones = new ArrayList<>();
        phones.addAll(toPhones(phoneNumbers, "1"));
        phones.addAll(toPhones(mobileNumbers, "3"));

        PhonesType phonesType = new PhonesType();
        phonesType.getPhones().addAll(phones);
        return Optional.of(phonesType);
    }

    private static List<Phone> toPhones(Set<String> numbers, String phoneTechType) {
        return numbers.stream()
                .map(number -> {
                    Phone phone = new Phone();
                    phone.setPhoneNumber(number);
                    phone.setPhoneTechType(phoneTechType);
                    return phone;
                })
                .toList();
    }

    private static Optional<EmailsType> extractEmailsType(Accommodation accommodation) {
        if (accommodation.getAccoDetailMap() == null || accommodation.getAccoDetailMap().isEmpty()) {
            return Optional.empty();
        }

        List<Email> emails = accommodation.getAccoDetailMap().values().stream()
                .map(AccoDetail::getEmail)
                .filter(Objects::nonNull)
                .filter(ContactInfosTypeBuilder::isStringNonEmpty)
                .distinct()
                .map(email -> {
                    Email result = new Email();
                    result.setEmailType("5");
                    result.setValue(email);
                    return result;
                })
                .toList();

        // If there are no emails, return an empty Optional
        if (emails.isEmpty()) {
            return Optional.empty();
        }

        EmailsType emailsType = new EmailsType();
        emailsType.getEmails().addAll(emails);
        return Optional.of(emailsType);
    }

    private static Optional<URLsType> extractURLsType(Accommodation accommodation) {
        URLsType urLsType = new URLsType();

        // Handle Website URLs
        if (accommodation.getAccoDetailMap() != null) {
            List<URL> urls = accommodation.getAccoDetailMap().values().stream()
                    .map(AccoDetail::getWebsite)
                    .filter(Objects::nonNull)
                    .filter(ContactInfosTypeBuilder::isStringNonEmpty)
                    .distinct()
                    .map(website -> {
                        URL result = new URL();
                        result.setValue(website);
                        return result;
                    })
                    .toList();

            urLsType.getURLS().addAll(urls);
        }

        // Handle TrustYou
        if (accommodation.getTrustYouId() != null
                && Boolean.TRUE.equals(accommodation.getTrustYouActive())
                && accommodation.getTrustYouState() == 2
        ) {
            String trustYouUrl = "https://www.trustyou.com/about/meta-review?ty_id=" + accommodation.getTrustYouId();
            URL url = new URL();
            url.setValue(trustYouUrl);
            url.setID("TRUSTYOU");

            urLsType.getURLS().add(url);
        }

        return urLsType.getURLS().isEmpty() ? Optional.empty() : Optional.of(urLsType);
    }

    private static boolean isContactInfoRootTypeEmpty(ContactInfoRootType contactInfoRootType) {
        return contactInfoRootType.getAddresses() == null
                && contactInfoRootType.getPhones() == null
                && contactInfoRootType.getEmails() == null
                && contactInfoRootType.getURLs() == null;
    }

    private static boolean isStringNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
