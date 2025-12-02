// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.common;

import it.bz.opendatahub.alpinebits.common.constants.OTACodePictureCategoryCode;
import it.bz.opendatahub.alpinebits.xml.schema.ota.CategoryCodesType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.CategoryCodesType.HotelCategory;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType.Descriptions;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType.Descriptions.MultimediaDescriptions;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType.Position;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType.Services;
import it.bz.opendatahub.alpinebits.xml.schema.ota.HotelInfoType.Services.Service;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageDescriptionType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageDescriptionType.ImageFormat;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageItemsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageItemsType.ImageItem;
import it.bz.opendatahub.alpinebits.xml.schema.ota.MultimediaDescriptionType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextDescriptionType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextItemsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextItemsType.TextItem;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.AccoDetail;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.Accommodation;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.ImageGalleryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper class to build {@link HotelInfoType} instances.
 */
public final class HotelInfoTypeBuilder {

    private HotelInfoTypeBuilder() {
        // Empty
    }

    /**
     * Try to extract a {@link HotelInfoType} from the given {@link Accommodation}.
     *
     * @param accommodation                     The accommodation data is used to extract a HotelInfoType instance.
     * @param withExtendedHotelInfoServiceCodes If this parameter is set to <code>true</code>, then extended service
     *                                          codes are included as <code>CodeDetail</code> elements and
     *                                          the <code>ExistsCode</code> element is set to "5".
     * @return An {@link Optional} wrapping the HotelInfoType if such an instance could be
     * extracted from the given accommodation, {@link Optional#empty()} otherwise.
     */
    public static Optional<HotelInfoType> extractHotelInfoType(Accommodation accommodation, boolean withExtendedHotelInfoServiceCodes) {
        if (accommodation == null) {
            return Optional.empty();
        }

        HotelInfoType hotelInfoType = new HotelInfoType();

        extractCategoryCodes(accommodation).ifPresent(hotelInfoType::setCategoryCodes);
        extractDescriptions(accommodation).ifPresent(hotelInfoType::setDescriptions);
        extractPosition(accommodation).ifPresent(hotelInfoType::setPosition);
        extractServices(accommodation, withExtendedHotelInfoServiceCodes).ifPresent(hotelInfoType::setServices);

        // Check if any data could be extracted from Accommodation
        // If none was extracted, then the whole HotelInfoType should be empty
        if (isHotelInfoTypeEmpty(hotelInfoType)) {
            return Optional.empty();
        }

        return Optional.of(hotelInfoType);
    }

    private static Optional<CategoryCodesType> extractCategoryCodes(Accommodation accommodation) {
        if (accommodation.getAccoTypeId() == null || accommodation.getAccoCategoryId() == null) {
            return Optional.empty();
        }

        String codeDetail = "ODH_" + accommodation.getAccoTypeId() + ":" + accommodation.getAccoCategoryId();

        HotelCategory hotelCategory = new HotelCategory();
        hotelCategory.setCodeDetail(codeDetail);

        CategoryCodesType categoryCodesType = new CategoryCodesType();
        categoryCodesType.getHotelCategories().add(hotelCategory);

        return Optional.of(categoryCodesType);
    }

    private static Optional<Descriptions> extractDescriptions(Accommodation accommodation) {
        if (accommodation.getAccoDetailMap() == null || accommodation.getAccoDetailMap().isEmpty()) {
            return Optional.empty();
        }

        List<MultimediaDescriptionType> multimediaDescriptionsList = new ArrayList<>();

        buildTextItemList(accommodation, "1", AccoDetail::getLongdesc).ifPresent(multimediaDescriptionsList::add);
        buildTextItemList(accommodation, "17", AccoDetail::getShortdesc).ifPresent(multimediaDescriptionsList::add);
        buildImageItemList(accommodation, "23").ifPresent(multimediaDescriptionsList::add);

        // If no descriptions were found, we're done and an empty Optional is the result
        if (multimediaDescriptionsList.isEmpty()) {
            return Optional.empty();
        }

        MultimediaDescriptions multimediaDescriptions = new MultimediaDescriptions();
        multimediaDescriptions.getMultimediaDescriptions().addAll(multimediaDescriptionsList);

        Descriptions descriptions = new Descriptions();
        descriptions.setMultimediaDescriptions(multimediaDescriptions);

        return Optional.of(descriptions);
    }

    private static Optional<MultimediaDescriptionType> buildTextItemList(
            Accommodation accommodation,
            String infoCode,
            Function<AccoDetail, String> textExtractor
    ) {
        List<TextItem> textItemList = accommodation.getAccoDetailMap().entrySet().stream()
                .map(entry -> {
                    String language = entry.getKey();
                    AccoDetail accoDetail = entry.getValue();
                    String description = textExtractor.apply(accoDetail);
                    return buildTextItem(language, description);
                })
                .filter(Objects::nonNull)
                .toList();

        if (textItemList.isEmpty()) {
            return Optional.empty();
        }

        TextItemsType textItemsType = new TextItemsType();
        textItemsType.getTextItems().addAll(textItemList);

        MultimediaDescriptionType descriptionType = new MultimediaDescriptionType();
        descriptionType.setTextItems(textItemsType);
        descriptionType.setInfoCode(infoCode);

        return Optional.of(descriptionType);
    }

    private static TextItem buildTextItem(String language, String value) {
        if (isStringEmpty(value)) {
            return null;
        }

        TextDescriptionType.Description description = new TextDescriptionType.Description();
        description.setTextFormat("PlainText");
        description.setLanguage(language);
        description.setValue(value);

        TextItem textItem = new TextItem();
        textItem.getDescriptions().add(description);
        return textItem;
    }

    private static Optional<MultimediaDescriptionType> buildImageItemList(Accommodation accommodation, String infoCode) {
        if (accommodation.getImageGalleries() == null || accommodation.getImageGalleries().isEmpty()) {
            return Optional.empty();
        }

        List<ImageItem> imageItemList = accommodation.getImageGalleries().stream()
                .map(imageGalleryEntry -> {
                    ImageItem imageItem = new ImageItem();
                    imageItem.setCategory(OTACodePictureCategoryCode.EXTERIOR_VIEW.getCode());

                    ImageFormat imageFormat = buildImageFormat(imageGalleryEntry);
                    imageItem.getImageFormats().add(imageFormat);

                    List<ImageDescriptionType.Description> descriptions = imageGalleryEntry.getImageDescriptions().entrySet().stream()
                            .map(entry -> {
                                String language = entry.getKey();
                                String value = entry.getValue();

                                return buildImageDescription(language, value);
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    imageItem.getDescriptions().addAll(descriptions);

                    return imageItem;
                })
                .toList();

        ImageItemsType imageItemsType = new ImageItemsType();
        imageItemsType.getImageItems().addAll(imageItemList);

        MultimediaDescriptionType multimediaDescriptionType = new MultimediaDescriptionType();
        multimediaDescriptionType.setImageItems(imageItemsType);
        multimediaDescriptionType.setInfoCode(infoCode);

        return Optional.of(multimediaDescriptionType);
    }

    private static ImageFormat buildImageFormat(ImageGalleryEntry imageGalleryEntry) {
        ImageFormat imageFormat = new ImageFormat();
        imageFormat.setURL(imageGalleryEntry.getImageUrl());

        List<String> copyrightNotices = new ArrayList<>();
        if (imageGalleryEntry.getLicense() != null) {
            copyrightNotices.add(imageGalleryEntry.getLicense());
        }
        if (imageGalleryEntry.getCopyRight() != null) {
            copyrightNotices.add(imageGalleryEntry.getCopyRight());
        }
        imageFormat.setCopyrightNotice(String.join(" - ", copyrightNotices));
        return imageFormat;
    }

    private static ImageDescriptionType.Description buildImageDescription(String language, String value) {
        if (isStringEmpty(value)) {
            return null;
        }

        ImageDescriptionType.Description description = new ImageDescriptionType.Description();
        description.setTextFormat("PlainText");
        description.setLanguage(language);
        description.setValue(value);
        return description;
    }

    private static Optional<Position> extractPosition(Accommodation accommodation) {
        Position position = new Position();
        boolean hasValue = false;

        // Both latitude and longitude must be present to be included in AlpineBits
        // Having only one value is not valid
        if (accommodation.getLatitude() != null && accommodation.getLongitude() != null) {
            position.setLatitude(accommodation.getLatitude().toString());
            position.setLongitude(accommodation.getLongitude().toString());
            hasValue = true;
        }

        if (accommodation.getAltitude() != null) {
            position.setAltitude(accommodation.getAltitude().toString());
            // Unit of measure is meter which means the value is "3"
            position.setAltitudeUnitOfMeasureCode("3");
            hasValue = true;
        }

        return hasValue ? Optional.of(position) : Optional.empty();
    }

    private static Optional<Services> extractServices(Accommodation accommodation, boolean withExtendedHotelInfoServiceCodes) {
        if (accommodation.getFeatures() == null) {
            return Optional.empty();
        }

        List<Service> serviceList = accommodation.getFeatures().stream()
                .map(feature -> {

                    if (!isStringEmpty(feature.getOtaCodes())) {
                        Service service = new Service();
                        service.setCode(feature.getOtaCodes());
                        service.setProximityCode("4");
                        return service;
                    } else if (withExtendedHotelInfoServiceCodes) {
                        Service service = new Service();
                        service.setCode("1");
                        service.setProximityCode("4");
                        // Set ExistsCode to "5", which means "Substitute", see Option Type Code (OTC)
                        service.setExistsCode("5");

                        if (!isStringEmpty(feature.getHgvId())) {
                            service.setCodeDetail("HGV_" + feature.getHgvId());
                        } else {
                            service.setCodeDetail("LTS_" + feature.getId());
                            return service;
                        }
                        return service;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        if (serviceList.isEmpty()) {
            return Optional.empty();
        }

        Services services = new Services();
        services.getServices().addAll(serviceList);

        return Optional.of(services);
    }

    private static boolean isHotelInfoTypeEmpty(HotelInfoType hotelInfoType) {
        return hotelInfoType.getCategoryCodes() == null
                && hotelInfoType.getDescriptions() == null
                && hotelInfoType.getPosition() == null
                && hotelInfoType.getServices() == null;
    }

    private static boolean isStringEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
