package xyz.andrick.figures.records;

public record PexelResponse(
        int page,
        int per_page,
        PexelImage[] photos,
        int total_results
) {

}
