CONTROLLER:

@RestController
public class PartnerDataController {

    @Autowired
    private PartnerDataService partnerDataService;

    @GetMapping(value = "XPTO", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<QueryPartnerDataResponseDTO>> queryPartnerData(
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "fileReport", required = false) String fileReport,
            @RequestParam(value = "benefitsFlow", required = false) String benefitsFlow,
            @RequestParam(value = "benefitsOrderId", required = false) String benefitsOrderId,
            @RequestParam(value = "searchedDate", required = false) String searchedDate,
            @RequestParam(value = "reportDate", required = false) String reportDate,
            @RequestParam(value = "subTag", required = false) String subTag,
            @RequestParam(value = "grossValue", required = false) String grossValue,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "sort", required = false, defaultValue = "ASC") String sort
            ){

        QueryPartnerDataRequestDTO queryPartner = QueryPartnerDataRequestDTO.builder()
                .customerId(customerId)
                .fileReport(fileReport)
                .benefitsFlow(benefitsFlow)
                .benefitsOrderId(benefitsOrderId)
                .searchedDate(searchedDate)
                .reportDate(reportDate)
                .subTag(subTag)
                .grossValue(grossValue)
                .startDate(startDate)
                .endDate(endDate)
                .size(size)
                .page(page)
                .sort(sort)
                .build();

        Page<QueryPartnerDataResponseDTO> responseDTO = partnerDataService.queryPartner(queryPartner);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }




SERVICE:
public Page<QueryPartnerDataResponseDTO> queryPartner(QueryPartnerDataRequestDTO queryRequestDTO) {
		log.info("queryPartner - start - request: [{}]", queryRequestDTO);

		PageRequest pageRequest = PageRequest.of(queryRequestDTO.getPage(), queryRequestDTO.getSize(),
				Sort.Direction.valueOf(queryRequestDTO.getSort()), "createdDate");

		Page<PartnerDataEntity> resultPage = partnerDataRepository.findAll(
				QueryTaskPartnerDataSpecification.createCriteria(queryRequestDTO), pageRequest);

		return resultPage.map(QueryPartnerDataResponseDTO::new);
	}


pasta SPECIFICATIONS:

package br.com.livelo.benefitsrepositoryamz.specifications;

import br.com.livelo.benefitsrepositoryamz.constants.Constants;
import br.com.livelo.benefitsrepositoryamz.dto.QueryPartnerDataRequestDTO;
import br.com.livelo.benefitsrepositoryamz.entities.PartnerDataEntity;
import br.com.livelo.benefitsrepositoryamz.exceptions.BusinessException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class QueryTaskPartnerDataSpecification {

    public static Specification<PartnerDataEntity> createCriteria(QueryPartnerDataRequestDTO requestFilter){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = createPredicates(requestFilter, root, criteriaBuilder);
            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
    }

    public static List<Predicate> createPredicates(QueryPartnerDataRequestDTO requestFilter, Root<PartnerDataEntity> root,
                                                   CriteriaBuilder criteriaBuilder){
        List<Predicate> predicates = new ArrayList<>();

        if(StringUtils.isNotBlank(requestFilter.getCustomerId())) {
            predicates.add(criteriaBuilder.equal(root.get("customerId"), requestFilter.getCustomerId()));
        }

        if (StringUtils.isNotBlank(requestFilter.getFileReport())) {
            predicates.add(criteriaBuilder.equal(root.get("fileReport"), requestFilter.getFileReport()));
        }

        if (StringUtils.isNotBlank(requestFilter.getBenefitsFlow())) {
            predicates.add(criteriaBuilder.equal(root.get("benefitsFlow"), requestFilter.getBenefitsFlow()));
        }

        if (StringUtils.isNotBlank(requestFilter.getBenefitsOrderId())) {
            predicates.add(criteriaBuilder.equal(root.get("benefitsOrderId"), requestFilter.getBenefitsOrderId()));
        }

        if (StringUtils.isNotBlank(requestFilter.getSearchedDate())) {
            predicates.add(criteriaBuilder.equal(root.get("searchedDate"), requestFilter.getSearchedDate()));
        }

        if (StringUtils.isNotBlank(requestFilter.getReportDate())) {
            predicates.add(criteriaBuilder.equal(root.get("reportDate"), requestFilter.getReportDate()));
        }

        if (StringUtils.isNotBlank(requestFilter.getSubTag())) {
            predicates.add(criteriaBuilder.equal(root.get("subTag"), requestFilter.getSubTag()));
        }

        if (StringUtils.isNotBlank(requestFilter.getGrossValue())) {
            predicates.add(criteriaBuilder.equal(root.get("grossValue"), requestFilter.getGrossValue()));
        }

        if((requestFilter.getStartDate() != null && requestFilter.getEndDate() == null) ||
                (requestFilter.getStartDate() == null && requestFilter.getEndDate() != null)){
            throw new BusinessException("startDate and endDate must be provided together");
        }

        if(requestFilter.getStartDate() != null && requestFilter.getEndDate() != null){
            ZonedDateTime startDate = convertIsoDateToZonedDateTime(requestFilter.getStartDate(), 0, 0, 0);
            ZonedDateTime endDate = convertIsoDateToZonedDateTime(requestFilter.getEndDate(), 23, 59, 59);

            if(endDate.isBefore(startDate)){
                throw new BusinessException("The endDate must be greater than or equal to startDate");
            }
            predicates.add(criteriaBuilder.between(root.get("createdDate"), startDate.toLocalDateTime(), endDate.toLocalDateTime()));
        }

        return predicates;
    }

    private static ZonedDateTime convertIsoDateToZonedDateTime(
            LocalDate localDate, Integer hour, Integer minute, Integer second){
        return ZonedDateTime.of(localDate.atTime(hour, minute, second), Constants.BRAZIL_ZONE_ID);
    }
}






