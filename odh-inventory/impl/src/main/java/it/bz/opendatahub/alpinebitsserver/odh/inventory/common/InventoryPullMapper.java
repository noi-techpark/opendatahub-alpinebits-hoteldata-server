// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.common;

import it.bz.opendatahub.alpinebits.common.constants.OTACodeGuestRoomInfo;
import it.bz.opendatahub.alpinebits.common.constants.OTACodeInformationType;
import it.bz.opendatahub.alpinebits.common.constants.OTACodePictureCategoryCode;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType.GuestRooms;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType.GuestRooms.GuestRoom;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType.GuestRooms.GuestRoom.Amenities;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType.GuestRooms.GuestRoom.Amenities.Amenity;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FacilityInfoType.GuestRooms.GuestRoom.TypeRoom;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageDescriptionType.ImageFormat;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageItemsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ImageItemsType.ImageItem;
import it.bz.opendatahub.alpinebits.xml.schema.ota.MultimediaDescriptionType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.MultimediaDescriptionsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS.HotelDescriptiveContents;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS.HotelDescriptiveContents.HotelDescriptiveContent;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextDescriptionType.Description;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextItemsType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.TextItemsType.TextItem;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.AccommodationRoom;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mapping utility class for inventory pull actions.
 */
public class InventoryPullMapper {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryPullMapper.class);

    /**
     * Map a list of {@link AccommodationRoom} elements to a
     * {@link OTAHotelDescriptiveInfoRS} element for the Inventory Basic
     * pull action.
     *
     * @param rooms Map this list to a OTAHotelDescriptiveInfoRS element.
     * @return The mapped OTAHotelDescriptiveInfoRS element.
     */
    public OTAHotelDescriptiveInfoRS mapToHotelDescriptiveInfoForBasic(List<AccommodationRoom> rooms) {
        List<GuestRoom> guestRooms = rooms
                .stream()
                .map(room -> {
                    // Compute guest room description used as room category
                    // description for AlpineBits GuestRooms
                    GuestRoom guestRoom = buildDescriptionGuestRoom(room);

                    // Compute list of real rooms
                    List<GuestRoom> realRooms = new ArrayList<>();
                    if (room.getRoomNumbers() != null) {
                        // Room numbers are available
                        for (String roomNumber : room.getRoomNumbers()) {
                            GuestRoom realRoom = this.buildRealGuestRoom(room, roomNumber);
                            realRooms.add(realRoom);
                        }
                    } else {
                        // No room numbers defined. Fall back to simple counter implementation.
                        // Note, that the generated room numbers usually are unknown top the hotel
                        // Also note, that the Inventory AlpineBits response doesn't contain a warning
                        // element. Otherwise it would be a good idea to add a warning to the response
                        LOG.warn("No room numbers found for room with ID {}. Falling back to counter", room.getId());

                        for (int i = 0; i < room.getRoomQuantity(); i++) {
                            String roomNumber = guestRoom.getCode() + "-" + i;
                            GuestRoom realRoom = this.buildRealGuestRoom(room, roomNumber);
                            realRooms.add(realRoom);
                        }
                    }
                    // Sort by RoomID (if such a value exists)
                    realRooms.sort(Comparator.comparing(o -> o.getTypeRooms().isEmpty() ? "" : o.getTypeRooms().getFirst().getRoomID()));

                    // Concatenate room category information and all real rooms
                    List<GuestRoom> allRooms = new ArrayList<>();
                    allRooms.add(guestRoom);
                    allRooms.addAll(realRooms);

                    return allRooms;
                })
                .flatMap(Collection::stream)
                .toList();

        return buildOtaHotelDescriptiveInfo(guestRooms);
    }

    /**
     * Map a list of {@link AccommodationRoom} elements to a
     * {@link OTAHotelDescriptiveInfoRS} element for the Inventory HotelInfo
     * pull action.
     *
     * @param rooms Map this list to a OTAHotelDescriptiveInfoRS element.
     * @return The mapped OTAHotelDescriptiveInfoRS element.
     */
    public OTAHotelDescriptiveInfoRS mapToHotelDescriptiveInfoForHotelInfo(List<AccommodationRoom> rooms) {
        List<GuestRoom> guestRooms = rooms
                .stream()
                .map(room -> {
                    GuestRoom guestRoom = new GuestRoom();
                    guestRoom.setCode(room.getRoomcode());

                    MultimediaDescriptionsType multimediaDescriptionsType = new MultimediaDescriptionsType();
                    buildPictures(room).ifPresent(multimediaDescriptionsType.getMultimediaDescriptions()::add);

                    guestRoom.setMultimediaDescriptions(multimediaDescriptionsType);
                    return guestRoom;
                })
                .toList();

        return buildOtaHotelDescriptiveInfo(guestRooms);
    }

    private OTAHotelDescriptiveInfoRS buildOtaHotelDescriptiveInfo(List<GuestRoom> guestRooms) {
        GuestRooms guestRooms1 = new GuestRooms();
        guestRooms1.getGuestRooms().addAll(guestRooms);

        FacilityInfoType facilityInfoType = new FacilityInfoType();
        facilityInfoType.setGuestRooms(guestRooms1);

        HotelDescriptiveContent hotelDescriptiveContentType = new HotelDescriptiveContent();
        hotelDescriptiveContentType.setFacilityInfo(facilityInfoType);

        HotelDescriptiveContents hotelDescriptiveContents = new HotelDescriptiveContents();
        hotelDescriptiveContents.getHotelDescriptiveContents().add(hotelDescriptiveContentType);

        OTAHotelDescriptiveInfoRS otaHotelDescriptiveInfoRS = new OTAHotelDescriptiveInfoRS();
        otaHotelDescriptiveInfoRS.setHotelDescriptiveContents(hotelDescriptiveContents);
        return otaHotelDescriptiveInfoRS;
    }

    private GuestRoom buildDescriptionGuestRoom(AccommodationRoom room) {
        GuestRoom guestRoom = new GuestRoom();
        guestRoom.setCode(room.getRoomcode());
        guestRoom.setMinOccupancy(toBigInt(room.getRoommin()));
        guestRoom.setMaxOccupancy(toBigInt(room.getRoommax()));
        guestRoom.getTypeRooms().add(buildTypeRoom(room));
        guestRoom.setMultimediaDescriptions(buildMultimediaDescriptions(room));
        guestRoom.setAmenities(buildAmenities(room));
        return guestRoom;
    }

    private GuestRoom buildRealGuestRoom(AccommodationRoom room, String roomId) {
        GuestRoom guestRoom = new GuestRoom();
        guestRoom.setCode(room.getRoomcode());
        TypeRoom typeRoom = new TypeRoom();
        typeRoom.setRoomID(roomId);
        guestRoom.getTypeRooms().add(typeRoom);
        return guestRoom;
    }

    private TypeRoom buildTypeRoom(AccommodationRoom room) {
        TypeRoom typeRoom = new TypeRoom();
        typeRoom.setStandardOccupancy(toBigInt(room.getRoomstd()));

        // Use ODH RoomClassificationCode if set,
        // otherwise use default value
        if (room.getRoomClassificationCode() != null) {
            typeRoom.setRoomClassificationCode(room.getRoomClassificationCode().toString());
        } else {
            typeRoom.setRoomClassificationCode(OTACodeGuestRoomInfo.ROOM.getCode());
        }

        return typeRoom;
    }

    private MultimediaDescriptionsType buildMultimediaDescriptions(AccommodationRoom room) {
        MultimediaDescriptionsType multimediaDescriptionsType = new MultimediaDescriptionsType();

        buildLongNames(room).ifPresent(multimediaDescriptionsType.getMultimediaDescriptions()::add);
        buildDescriptions(room).ifPresent(multimediaDescriptionsType.getMultimediaDescriptions()::add);
        buildPictures(room).ifPresent(multimediaDescriptionsType.getMultimediaDescriptions()::add);

        return multimediaDescriptionsType;
    }


    private Amenities buildAmenities(AccommodationRoom room) {
        List<Amenity> amenitiesWithCode = room.getFeatures()
                .stream()
                .map(Feature::getRoomAmenityCodes)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .sorted(Comparator.comparingInt(Integer::intValue))
                .map(roomAmenityCode -> {
                    Amenity amenity = new Amenity();
                    amenity.setRoomAmenityCode(roomAmenityCode.toString());
                    return amenity;
                })
                .toList();

        Amenities amenities = new Amenities();
        amenities.getAmenities().addAll(amenitiesWithCode);
        return amenities;
    }

    private Optional<MultimediaDescriptionType> buildLongNames(AccommodationRoom room) {
        if (room.getAccoRoomDetailMap().isEmpty()) {
            return Optional.empty();
        }

        List<Description> descriptions = room.getAccoRoomDetailMap().values()
                .stream()
                .map(value -> {
                    Description description = new Description();
                    description.setLanguage(value.getLanguage());
                    description.setTextFormat("PlainText");
                    description.setValue(value.getName());
                    return description;
                })
                .toList();
        TextItem textItem = new TextItem();
        textItem.getDescriptions().addAll(descriptions);

        TextItemsType textItemsType = new TextItemsType();
        textItemsType.getTextItems().add(textItem);

        MultimediaDescriptionType multimediaDescriptionType = new MultimediaDescriptionType();
        multimediaDescriptionType.setTextItems(textItemsType);
        multimediaDescriptionType.setInfoCode(OTACodeInformationType.LONG_NAME.getCode());
        return Optional.of(multimediaDescriptionType);
    }

    private Optional<MultimediaDescriptionType> buildDescriptions(AccommodationRoom room) {
        if (room.getAccoRoomDetailMap().isEmpty()) {
            return Optional.empty();
        }

        List<Description> descriptions = room.getAccoRoomDetailMap().values()
                .stream()
                .map(value -> {
                    Description description = new Description();
                    description.setLanguage(value.getLanguage());
                    description.setTextFormat("PlainText");
                    description.setValue(value.getLongdesc());
                    return description;
                })
                .toList();
        TextItem textItem = new TextItem();
        textItem.getDescriptions().addAll(descriptions);

        TextItemsType textItemsType = new TextItemsType();
        textItemsType.getTextItems().add(textItem);

        MultimediaDescriptionType multimediaDescriptionType = new MultimediaDescriptionType();
        multimediaDescriptionType.setTextItems(textItemsType);
        multimediaDescriptionType.setInfoCode(OTACodeInformationType.DESCRIPTION.getCode());
        return Optional.of(multimediaDescriptionType);
    }


    private Optional<MultimediaDescriptionType> buildPictures(AccommodationRoom room) {
        if (room.getImageGalleryEntries().isEmpty()) {
            return Optional.empty();
        }

        List<ImageItem> imageItems = room.getImageGalleryEntries()
                .stream()
                .map(imageGallery -> {
                    ImageItem imageItem = new ImageItem();

                    // See OTA PIC 20 -> "Miscellaneous"
                    imageItem.setCategory(OTACodePictureCategoryCode.MISCELLANEOUS.getCode());

                    ImageFormat imageFormat = new ImageFormat();
                    imageFormat.setCopyrightNotice(imageGallery.getCopyRight());
                    imageFormat.setURL(imageGallery.getImageUrl());

                    imageItem.getImageFormats().add(imageFormat);

                    return imageItem;
                })
                .toList();

        ImageItemsType imageItemsType = new ImageItemsType();
        imageItemsType.getImageItems().addAll(imageItems);

        MultimediaDescriptionType multimediaDescriptionType = new MultimediaDescriptionType();
        multimediaDescriptionType.setImageItems(imageItemsType);
        multimediaDescriptionType.setInfoCode(OTACodeInformationType.PICTURES.getCode());
        return Optional.of(multimediaDescriptionType);
    }

    private BigInteger toBigInt(Integer value) {
        return value != null ? BigInteger.valueOf(value) : null;
    }

}
