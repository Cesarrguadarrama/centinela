package mx.centinela.bootstrap.api.dto;

import java.util.List;

/** Offset-paginated result. */
public record PageView<T>(List<T> items, int page, int size, long totalItems) {}
