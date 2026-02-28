package com.example.bvt.service;

import com.example.bvt.common.BusinessException;
import com.example.bvt.domain.TestCaseDataSetEntity;
import com.example.bvt.domain.TestCaseEntity;
import com.example.bvt.dto.DataSetCreateRequest;
import com.example.bvt.dto.DataSetResponse;
import com.example.bvt.engine.ConfigParserEngine;
import com.example.bvt.repository.TestCaseDataSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataSetService {

    private final TestCaseDataSetRepository dataSetRepository;
    private final TestCaseService testCaseService;
    private final ConfigParserEngine configParserEngine;

    public DataSetService(TestCaseDataSetRepository dataSetRepository,
                          TestCaseService testCaseService,
                          ConfigParserEngine configParserEngine) {
        this.dataSetRepository = dataSetRepository;
        this.testCaseService = testCaseService;
        this.configParserEngine = configParserEngine;
    }

    @Transactional
    public DataSetResponse create(Long caseId, DataSetCreateRequest request) {
        TestCaseEntity testCase = testCaseService.getEntity(caseId);
        configParserEngine.parseDataContent(request.dataFormat(), request.dataContent());
        if (request.isDefault()) {
            clearDefault(caseId);
        }
        TestCaseDataSetEntity entity = new TestCaseDataSetEntity();
        entity.setTestCase(testCase);
        entity.setDataSetName(request.dataSetName());
        entity.setDataFormat(request.dataFormat());
        entity.setDataContent(request.dataContent());
        entity.setDefault(request.isDefault());
        return toResponse(dataSetRepository.save(entity));
    }

    public List<DataSetResponse> list(Long caseId) {
        return dataSetRepository.findByTestCaseIdOrderByIdAsc(caseId).stream().map(this::toResponse).toList();
    }

    public TestCaseDataSetEntity getEntity(Long id) {
        return dataSetRepository.findById(id).orElseThrow(() -> new BusinessException("Data set not found: " + id));
    }

    private void clearDefault(Long caseId) {
        List<TestCaseDataSetEntity> list = dataSetRepository.findByTestCaseIdOrderByIdAsc(caseId);
        for (TestCaseDataSetEntity dataSet : list) {
            if (dataSet.isDefault()) {
                dataSet.setDefault(false);
                dataSetRepository.save(dataSet);
            }
        }
    }

    private DataSetResponse toResponse(TestCaseDataSetEntity entity) {
        return new DataSetResponse(
                entity.getId(),
                entity.getTestCase().getId(),
                entity.getDataSetName(),
                entity.getDataFormat(),
                entity.getDataContent(),
                entity.isDefault()
        );
    }
}
