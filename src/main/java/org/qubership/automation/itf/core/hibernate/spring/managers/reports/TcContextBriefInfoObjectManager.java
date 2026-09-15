/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.TcContextBriefInfoRepository;
import org.qubership.automation.itf.core.model.jpa.context.QTcContextBriefInfo;
import org.qubership.automation.itf.core.model.jpa.context.TcContextBriefInfo;
import org.qubership.automation.itf.core.util.constants.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import jakarta.annotation.PostConstruct;

@Service
public class TcContextBriefInfoObjectManager extends AbstractObjectManager<TcContextBriefInfo, TcContextBriefInfo>
        implements SearchByProjectIdManager<TcContextBriefInfo> {

    private static final Sort SORT_BY_START_TIME_DESC = Sort.by(new Sort.Order(Sort.Direction.DESC, "startTime"));

    private static TcContextBriefInfoRepository tcContextBriefInfoRepository;

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
     * Returns one page of {@link TcContextBriefInfo}, filtered by every non-empty criterion given
     * and sorted by {@code sortProperty} (defaulting to {@code ID} when blank; {@code "initiator"}
     * and {@code "environment"} are mapped to the underlying {@code ininame} and {@code envname}
     * columns).
     *
     * @param pageSize the page size
     * @param pageIndex the page index; treated as {@code 0} when {@code -1}
     * @param search unused
     * @param name filters on a case-insensitive substring of the context name
     * @param initiator filters on a case-insensitive substring of the initiator name
     * @param status filters on a case-insensitive substring of the status
     * @param environment filters on a case-insensitive substring of the environment name
     * @param stDate filters on the start time, compared per {@code startDateCondition}
     * @param startDateCondition {@code "more"}/{@code ">"}, {@code "less"}/{@code "<"}, or
     *     {@code "equals"}/{@code "="} (the default) against {@code stDate}
     * @param finDate filters on the end time, compared per {@code finishDateCondition}
     * @param finishDateCondition {@code "more"}/{@code ">"}, {@code "less"}/{@code "<"}, or
     *     {@code "equals"}/{@code "="} (the default) against {@code finDate}
     * @param duration filters on the duration, compared per {@code durationCondition}
     * @param durationCondition {@code "more"}/{@code ">"} or {@code "less"}/{@code "<"} against
     *     {@code duration}
     * @param client filters on a case-insensitive substring of the client name
     * @param sortProperty the property to sort by
     * @param sortOrder {@code true} for descending, {@code false} for ascending
     * @param projectId filters on the exact project id
     * @return the matching page
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
     * Returns every {@link TcContextBriefInfo} matching the given criteria, sorted the way
     * {@link #getPageByFilter} sorts, but unpaged.
     *
     * @return the matching contexts, sorted
     * @see #getPageByFilter for what each filter and sort parameter matches
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
     * Returns every {@link TcContextBriefInfo} whose initiator and environment names contain the
     * given substrings (case-insensitively), whose status matches when given, and whose end time is
     * at or after {@code minStartDate} and whose start time is at or before {@code maxStartDate}.
     *
     * @param initiator filters on a case-insensitive substring of the initiator name
     * @param status filters on a case-insensitive substring of the status, when not blank
     * @param environment filters on a case-insensitive substring of the environment name
     * @param minStartDate the earliest end time to include
     * @param maxStartDate the latest start time to include
     * @return the matching contexts, unsorted
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
     * Returns every {@link TcContextBriefInfo} matching the given criteria, unsorted and unpaged.
     * Used only to build the result set for a bulk delete: an empty predicate (every criterion
     * absent) returns an empty result rather than every context, so a filterless call cannot delete
     * everything by accident.
     *
     * @return the matching contexts, or an empty list when every criterion is absent
     * @see #getPageByFilter for what each filter parameter matches
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
     * Returns every {@link TcContextBriefInfo} whose status is not {@link Status#IN_PROGRESS}.
     *
     * @return the matching contexts, unsorted
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
