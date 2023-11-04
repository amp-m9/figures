package xyz.andrick.figures.records;

public record PexelImage(
        int id,
        int width,
        int height,
        String url,
        String photographer,
        String photographer_url,
        int photographer_id,
        String avg_color,
        image_source src,
        boolean liked,
        String alt

) {
    public record image_source(
            String original,
            String large2x,
            String large,
            String medium,
            String small,
            String tiny,
            String portrait,
            String landscape
    ) {
    }
}
