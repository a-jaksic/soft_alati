package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos;

public record RestaurantDetailsDTO(
        Long id,

        String name,

        String address,

        Double latitude,

        Double longitude,

        String phoneNum,

        String website,

        Integer reviewCount,

        Double avgRating,

        Long cityId,

        Long restaurantTypeId
)
{}
