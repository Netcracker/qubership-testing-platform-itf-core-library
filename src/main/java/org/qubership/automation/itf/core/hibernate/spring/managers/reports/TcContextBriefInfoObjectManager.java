/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.hibernate.spring.managers.reports;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.TcContextBriefInfoRepository;
import org.qubership.automation.itf.core.model.jpa.context.QTcContextBriefInfo;
import org.qubership.automation.itf.core.model.jpa.context.TcContextBriefInfo;
import org.qubership.automation.itf.core.util.constants.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

@Service
public class TcContextBriefInfoObjectManager extends AbstractObjectManager<TcContextBriefInfo, TcContextBriefInfo>
        implements SearchByProjectIdManager<TcContextBriefInfo> {

    private static final Sort SORT_BY_START_TIME_DESC = Sort.by(new Sort.Order(Sort.Direction.DESC, "startTime"));

    private static TcContextBriefInfoRepository tcContextBriefInfoRepository;

    @Autowired
    protected TcContextBriefInfoObjectManager(TcContextBriefInfoRepository repository) {
        super(TcContextBriefInfo.class, repository);
        tcContextBriefInfoRepository = repository;
    }

    @PostConstruct
    protected void init() {
    }

    /**
     * Get page of contexts.
     */
    public static Page<TcContextBriefInfo> getPage(int pageSize, int pageIndex, BigInteger projectId) {
        PageRequest pageable = PageRequest.of(pageIndex, pageSize, SORT_BY_START_TIME_DESC);
        BooleanExpression predicate = null;
        QTcContextBriefInfo contextInfo = QTcContextBriefInfo.tcContextBriefInfo;
        predicate = addBigIntegerExpression(predicate, contextInfo.projectId, projectId);
        return tcContextBriefInfoRepository.findAll(predicate, pageable);
    }

    private static BooleanExpression combineExpressions(BooleanExpression predicate, BooleanExpression expr) {
        if (predicate == null) {
            return expr;
        } else {
            return predicate.and(expr);
        }
    }

    private static BooleanExpression addStringExpression(BooleanExpression predicate, StringPath stringPath,
                                                         String str) {
        if (StringUtils.isNotBlank(str)) {
            String s = str.trim();
            if (!s.isEmpty()) {
                BooleanExpression expr = stringPath.containsIgnoreCase(s);
                return combineExpressions(predicate, expr);
            }
        }
        return predicate;
    }

    private static BooleanExpression addBigIntegerExpression(BooleanExpression predicate,
                                                             NumberPath<BigInteger> object, BigInteger value) {
        if (value != null) {
            BooleanExpression expr = object.eq(value);
            return combineExpressions(predicate, expr);
        }
        return predicate;
    }

    private static BooleanExpression addDateExpression(BooleanExpression predicate,
                                                       DateTimePath<Date> datePath, Date dat,
                                                       String condition) {
        if (dat != null) {
            BooleanExpression expr;
            switch (condition) {
                case "more":
                case ">":
                    expr = datePath.goe(dat);
                    break;
                case "less":
                case "<":
                    expr = datePath.loe(dat);
                    break;
                case "equals":
                case "=":
                default:
                    /*
                        date parameter (from UI) contains no time fields, so it points to the beginning of the day.
                     */
                    Date nextDay = new Date(dat.getTime() + (1000 * 60 * 60 * 24));
                    expr = datePath.goe(dat).and(datePath.lt(nextDay));
            }
            if (expr != null) {
                return combineExpressions(predicate, expr);
            }
        }
        return predicate;
    }

    private static BooleanExpression addDurationExpression(BooleanExpression predicate,
                                                           NumberPath<Long> durationPath,
                                                           Long duration,
                                                           String condition) {
        if (duration != null) {
            BooleanExpression expr = null;
            switch (condition) {
                case "more":
                case ">":
                    expr = durationPath.goe(duration);
                    break;
                case "less":
                case "<":
                    expr = durationPath.loe(duration);
                    break;
                default:
                    break;
            }
            if (expr != null) {
                return combineExpressions(predicate, expr);
            }
        }
        return predicate;
    }

    private static BooleanExpression makePredicate(String name,
                                                   String initiator,
                                                   String status,
                                                   String environment,
                                                   Date stDate,
                                                   String startDateCondition,
                                                   Date finDate,
                                                   String finishDateCondition,
                                                   Long duration,
                                                   String durationCondition,
                                                   String client,
                                                   boolean notRunningOnly,
                                                   BigInteger projectId) {
        QTcContextBriefInfo contextInfo = QTcContextBriefInfo.tcContextBriefInfo;
        BooleanExpression predicate = null;
        // Construct DSL expression based on parameter values...
        predicate = addStringExpression(predicate, contextInfo.name, name);
        predicate = addStringExpression(predicate, contextInfo.ininame, initiator);
        predicate = addStringExpression(predicate, contextInfo.envname, environment);
        predicate = addStringExpression(predicate, contextInfo.client, client);
        predicate = addDateExpression(predicate, contextInfo.startTime, stDate, startDateCondition);
        predicate = addDateExpression(predicate, contextInfo.endTime, finDate, finishDateCondition);
        predicate = addDurationExpression(predicate, contextInfo.duration, duration, durationCondition);
        predicate = addBigIntegerExpression(predicate, contextInfo.projectId, projectId);
        if (!StringUtils.isBlank(status)) {
            String s = status.trim().replace(' ', '_');
            if (!s.isEmpty()) {
                BooleanExpression statusExpr = contextInfo.status.stringValue().containsIgnoreCase(s);
                predicate = combineExpressions(predicate, statusExpr);
            }
        }
        if (notRunningOnly) {
            BooleanExpression notInProgressExpr = contextInfo.status.ne(Status.IN_PROGRESS);
            predicate = combineExpressions(predicate, notInProgressExpr);
        }
        return predicate;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Page<TcContextBriefInfo> getPageByFilter(int pageSize,
                                                           int pageIndex,
                                                           boolean search,
                                                           String name,
                                                           String initiator,
                                                           String status,
                                                           String environment,
                                                           Date stDate,
                                                           String startDateCondition,
                                                           Date finDate,
                                                           String finishDateCondition,
                                                           Long duration,
                                                           String durationCondition,
                                                           String client,
                                                           String sortProperty,
                                                           boolean sortOrder,
                                                           BigInteger projectId) {
        String sortBy;
        if (StringUtils.isNotEmpty(sortProperty)) {
            switch (sortProperty) {
                case "initiator":
                    sortBy = "ininame";
                    break;
                case "environment":
                    sortBy = "envname";
                    break;
                default:
                    sortBy = sortProperty;
            }
        } else {
            sortBy = "ID";
        }
        PageRequest pageable = PageRequest.of((pageIndex == -1) ? 0 : pageIndex, pageSize,
                Sort.by(new Sort.Order(sortOrder ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy)));
        BooleanExpression predicate = makePredicate(name, initiator, status, environment, stDate, startDateCondition,
                finDate, finishDateCondition, duration, durationCondition, client, false, projectId);
        if (predicate != null) {
            return tcContextBriefInfoRepository.findAll(predicate, pageable);
        } else {
            return tcContextBriefInfoRepository.findAll(pageable);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Iterable<TcContextBriefInfo> getReportByFilter(String name,
                                                                 String initiator,
                                                                 String status,
                                                                 String environment,
                                                                 Date stDate,
                                                                 String startDateCondition,
                                                                 Date finDate,
                                                                 String finishDateCondition,
                                                                 Long duration,
                                                                 String durationCondition,
                                                                 String client,
                                                                 String sortProperty,
                                                                 boolean sortOrder,
                                                                 BigInteger projectId) {
        String sortBy;
        if (StringUtils.isNotEmpty(sortProperty)) {
            switch (sortProperty) {
                case "initiator":
                    sortBy = "ininame";
                    break;
                case "environment":
                    sortBy = "envname";
                    break;
                default:
                    sortBy = sortProperty;
            }
        } else {
            sortBy = "ID";
        }

        BooleanExpression predicate = makePredicate(name, initiator, status, environment, stDate, startDateCondition,
                finDate, finishDateCondition, duration, durationCondition, client, false, projectId);

        if (predicate != null) {
            return tcContextBriefInfoRepository.findAll(predicate, Sort.by(new Sort.Order(sortOrder
                    ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy)));
        } else {
            return tcContextBriefInfoRepository.findAll(Sort.by(new Sort.Order(sortOrder ? Sort.Direction.DESC :
                    Sort.Direction.ASC, sortBy)));
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Iterable<TcContextBriefInfo> simpleSearch(String initiator, String status, String environment,
                                                            Date minStartDate, Date maxStartDate) {
        QTcContextBriefInfo contextInfo = QTcContextBriefInfo.tcContextBriefInfo;
        BooleanExpression predicate = null;
        predicate = addStringExpression(predicate, contextInfo.ininame, initiator);
        predicate = addStringExpression(predicate, contextInfo.envname, environment);
        predicate = addDateExpression(predicate, contextInfo.endTime, minStartDate, "more");
        predicate = addDateExpression(predicate, contextInfo.startTime, maxStartDate, "less");
        if (!StringUtils.isBlank(status)) {
            String s = status.trim().replace(' ', '_');
            if (!s.isEmpty()) {
                BooleanExpression statusExpr = contextInfo.status.stringValue().containsIgnoreCase(s);
                predicate = combineExpressions(predicate, statusExpr);
            }
        }
        return tcContextBriefInfoRepository.findAll(predicate);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Iterable<TcContextBriefInfo> findByFilter(String name,
                                                            String initiator,
                                                            String status,
                                                            String environment,
                                                            Date stDate,
                                                            String startDateCondition,
                                                            Date finDate,
                                                            String finishDateCondition,
                                                            Long duration,
                                                            String durationCondition,
                                                            String client,
                                                            BigInteger projectId) {
        BooleanExpression predicate = makePredicate(name, initiator, status, environment, stDate, startDateCondition,
                finDate, finishDateCondition, duration, durationCondition, client, true, projectId);
        if (predicate != null) {
            /*
                This method is used for deleteByFilter ONLY.
                So we return not Pages but All query results without any sorting.
             */
            return tcContextBriefInfoRepository.findAll(predicate);
        } else {
            /*
                If predicate is empty, we should - I think - return empty list.
                 Because we should NOT delete ALL contexts this way.
             */
            return new ArrayList<>();
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Iterable<TcContextBriefInfo> findNotRunning() {
        QTcContextBriefInfo contextInfo = QTcContextBriefInfo.tcContextBriefInfo;
        BooleanExpression notInProgressExpr = contextInfo.status.ne(Status.IN_PROGRESS);
        return tcContextBriefInfoRepository.findAll(notInProgressExpr);
    }

    public static Iterable<TcContextBriefInfo> findAll() {
        return tcContextBriefInfoRepository.findAll();
    }

    @Override
    public Collection<TcContextBriefInfo> getByProjectId(BigInteger projectId) {
        return tcContextBriefInfoRepository.findByProjectId(projectId);
    }

    public TcContextBriefInfo getByIDAndPartNum(BigInteger id, Integer partNum) {
        return tcContextBriefInfoRepository.findByIDAndPartNum(id, partNum);
    }

    public static int getCurrentPartitionNumber() {
        return tcContextBriefInfoRepository.getCurrentPartitionNumber();
    }
}
