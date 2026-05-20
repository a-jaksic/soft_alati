package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos;

public record RestaurantDTO(
        Long id,

        String name,

        String cityName,

        String typeName,

        Double avgRating,

        String address,

        String phoneNum,

        String website
)
{}
