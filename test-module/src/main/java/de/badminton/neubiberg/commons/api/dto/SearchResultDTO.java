package de.badminton.neubiberg.commons.api.dto;

import lombok.Data;

import java.util.List;

/**
 * A DTO representing the result of a search.
 * These DTOs are used to send only a part of
 * the search result and allow the client to
 * extend the search needle for a more details search.
 *
 * @param <T> the type of the results
 */
@Data
public class SearchResultDTO<T> {

    private List<T> result;
    private int hits;

}
