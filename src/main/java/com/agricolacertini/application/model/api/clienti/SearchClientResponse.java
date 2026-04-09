package com.agricolacertini.application.model.api.clienti;

import com.agricolacertini.application.model.api.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchClientResponse {

    private List<SearchClientsDto> searchClientsDtoList;
    private PageInfo pageInfo;
}
