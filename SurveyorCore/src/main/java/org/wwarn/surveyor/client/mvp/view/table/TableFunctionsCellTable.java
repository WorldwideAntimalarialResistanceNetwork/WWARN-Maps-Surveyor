package org.wwarn.surveyor.client.mvp.view.table;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import org.wwarn.surveyor.client.core.DataType;
import org.wwarn.surveyor.client.core.RecordList;

import java.util.Date;

/**
 * Created by suay on 7/8/14.
 */
public class TableFunctionsCellTable {

    String function;

    RecordList.Record record;

    TableFunctions.FunctionType functionType;

    String params[];

    public TableFunctionsCellTable(String function, RecordList.Record record){
        this.function = TableFunctions.cleanFunction(function);
        this.functionType = TableFunctions.getFunctionType(this.function);
        this.record = record;
        this.params = TableFunctions.getParameters(functionType, this.function);
    }

    public String resolve(){
        String result = null;
        if(functionType == TableFunctions.FunctionType.CONCAT_DATE){
            result = concatDate();
        }else if(functionType == TableFunctions.FunctionType.ARITH){
            result = arith();
        }else if(functionType == TableFunctions.FunctionType.GET_YEAR){
            result = getYear();
        }else if(functionType == TableFunctions.FunctionType.IF_NULL){
            result = ifNull();
        }else if(functionType == TableFunctions.FunctionType.LIMIT_STRING){
            result = limitString();
        }
        return result;
    }



    /**
     * Given 2 fields, concat two years eg 2006 - 2008
     * In the config file use:
     * fieldName="func(CONCAT_DATE(StudyFrom,StudyTo))"
     */
    private String concatDate(){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for concat date expression");
        }

        final DateTimeFormat yearFormat = DateTimeFormat.getFormat(DataType.DATE_FORMAT_YEAR_ONLY);

        final String rawDateFrom = record.getValueByFieldName(params[0]);
        Date dateFrom = DataType.ParseUtil.tryParseDate(rawDateFrom, DataType.DEFAULT_DATE);
        final String rawDateTo = record.getValueByFieldName(params[1]);
        Date dateTo = DataType.ParseUtil.tryParseDate(rawDateTo, DataType.DEFAULT_DATE);


        String res;
        if(dateFrom.equals(dateTo)){
            res = yearFormat.format(dateFrom);
        }else{
            res = yearFormat.format(dateFrom) + " - " + yearFormat.format(dateTo);
        }
        return res;
    }

    /**
     * Calculate an arithmetic expression
     * In the config file use:
     * fieldName="func(ARITH(field1,field2,operator))"
     */
    private String arith(){

        if(params.length != 3){
            throw new IllegalArgumentException("Wrong number of Parameters for arithmetic expression");
        }

        double res = 0;
        try{
            String operator = params[2];

            double firstValue = Double.parseDouble(record.getValueByFieldName(params[0]));

            double secondValue = Double.parseDouble(record.getValueByFieldName(params[1]));


            if("+".equals(operator)){
                res = firstValue + secondValue;
            }else if("-".equals(operator)){
                res = firstValue - secondValue;
            }else if("*".equals(operator)){
                res = firstValue * secondValue;
            }else if("/".equals(operator)){
                res = firstValue / secondValue;
            }else if("%".equals(operator)){
                res = (firstValue / secondValue) * 100;
            }

            NumberFormat decimalFormat = NumberFormat.getFormat(".##");
            res = Double.parseDouble(decimalFormat.format(res));
        }catch(Exception e){
            System.err.println("Error calculating table arithmetic function");
        }
        return Double.toString(res);

    }

    private String getYear(){
        if(params.length != 1){
            throw new IllegalArgumentException("Wrong number of Parameters for concat date expression");
        }

        final DateTimeFormat yearFormat = DateTimeFormat.getFormat(DataType.DATE_FORMAT_YEAR_ONLY);

        final String rawDateFrom = record.getValueByFieldName(params[0]);
        Date dateFrom = DataType.ParseUtil.tryParseDate(rawDateFrom, DataType.DEFAULT_DATE);

        String yearString = yearFormat.format(dateFrom);
        return yearString;
    }
    /**
     * Given 2 fields, if the first is null then return the second field
     * In the config file use:
     * fieldName="func(IFNULL(field1,field2))"
     */
    private String ifNull(){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for if null expression");
        }

        final String field1 = record.getValueByFieldName(params[0]);
        if (!field1.isEmpty()){
            return field1;
        }
        final String field2 = record.getValueByFieldName(params[1]);
        return field2;
    }

    /**
     * Reduce a String to a max limit
     * In the config file use:
     * fieldName="func(Limit(field,limit))"
     */
    private String limitString() {
        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for limit string expression");
        }

        final String field = record.getValueByFieldName(params[0]);
        int limit = Integer.parseInt(params[1]);

        if (field.isEmpty() || field.length() < limit){
            return field;
        }

        String result = field.substring(0, limit) + "...";
        return result;

    }




}
