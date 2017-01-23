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
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.wwarn.surveyor.client.core.DataSchema;
import org.wwarn.surveyor.client.core.DataType;
import org.wwarn.surveyor.client.core.RecordList;

import java.util.Date;

/**
 * Created by suay on 5/27/14.
 * This class will be used to resolve functions in the Table
 * Arithmetic or String functions could be used here.
 *
 * e.g. function = "func(ARITH(present,tested,/)"
 */
public class TableFunctions {

    String function;

    FunctionType functionType;

    RecordList recordList;

    String params[];

    DataTable table;

    RecordList.Record record;


    public TableFunctions(String function, RecordList recordList, DataTable table){
        this.function = cleanFunction(function);
        this.recordList = recordList;
        this.table = table;
        this.functionType = getFunctionType(this.function);
        this.params = getParameters(functionType, this.function);

    }

    public TableFunctions(String function, RecordList.Record record){
        this.function = cleanFunction(function);
        this.record = record;
    }



    /**
     * remove space, (, ), "func"
     * eg func(CONCAT(a1,a2))
     * returns CONCAT a1,a2
     */
    public static String cleanFunction(String function){
        //
        String regex = "func|[() ]";
        String cleanedfunc = function.replaceAll(regex, "");
        return cleanedfunc;

    }

    public static FunctionType getFunctionType(String function){
        for (FunctionType funcType : FunctionType.values() ){
            if (function.startsWith(funcType.name())){
                return funcType;
            }
        }
        return null;
    }

    public void resolve(int rowIndex, int tableColumnIndex){
        if(functionType == FunctionType.CONCAT_DATE){
            table.setValue(rowIndex, tableColumnIndex, concatDate(rowIndex));
        }else if(functionType == FunctionType.ARITH){
            table.setValue(rowIndex, tableColumnIndex, arith(rowIndex));
        }else if(functionType == FunctionType.IF_NULL){
            table.setValue(rowIndex, tableColumnIndex, ifNull(rowIndex));
        }else if(functionType == FunctionType.LIMIT_STRING){
            table.setValue(rowIndex, tableColumnIndex, limitString(rowIndex));
        }else if(functionType == FunctionType.PUBMED_URL){
            table.setValue(rowIndex, tableColumnIndex, pubMedURL(rowIndex));
        }
    }

    /**
     * Given 2 fields, concat two years eg 2006 - 2008
     * In the config file use:
     * fieldName="func(CONCAT_DATE(StudyFrom,StudyTo))"
     */
    private String concatDate(int rowIndex){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for concat date expression");
        }

        final DateTimeFormat yearFormat = DateTimeFormat.getFormat(DataType.DATE_FORMAT_YEAR_ONLY);
        final RecordList.Record currentRecord = recordList.getRecords().get(rowIndex);
        final String rawDateFrom = currentRecord.getValueByFieldName(params[0]);
        Date dateFrom = DataType.ParseUtil.tryParseDate(rawDateFrom, DataType.DEFAULT_DATE);
        final String rawDateTo = currentRecord.getValueByFieldName(params[1]);
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
    private double arith(int rowIndex){

        if(params.length != 3){
            throw new IllegalArgumentException("Wrong number of Parameters for arithmetic expression");
        }

        double res = 0;
        try{
            final RecordList.Record currentRecord = recordList.getRecords().get(rowIndex);
            String operator = params[2];

            double firstValue = Double.parseDouble(currentRecord.getValueByFieldName(params[0]));

            double secondValue = Double.parseDouble(currentRecord.getValueByFieldName(params[1]));


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
        return res;

    }

    /**
     * Given 2 fields, if the first is null then return the second field
     * In the config file use:
     * fieldName="func(IFNULL(field1,field2))"
     */
    private String ifNull(int rowIndex){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for if null expression");
        }

        final RecordList.Record currentRecord = recordList.getRecords().get(rowIndex);
        final String field1 = currentRecord.getValueByFieldName(params[0]);

        if (!field1.isEmpty()){
            return field1;
        }
        final String field2 = currentRecord.getValueByFieldName(params[1]);
        return field2;
    }

    /**
     * Reduce a String to a max limit
     * In the config file use:
     * fieldName="func(Limit(field,limit))"
     */
    private String limitString(int rowIndex){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for limit string expression");
        }

        final RecordList.Record currentRecord = recordList.getRecords().get(rowIndex);
        final String field = currentRecord.getValueByFieldName(params[0]);
        int limit = Integer.parseInt(params[1]);

        if (field.isEmpty() || field.length() < limit){
            return field;
        }

        String result = field.substring(0, limit) + "...";
        return result;
    }

       /**
     * Create an anchor with the title and the pubMedId
     * In the config file use:
     * fieldName="func(PUBMED_URL(title,pubMedID))"
     */
    private String pubMedURL(int rowIndex){

        if(params.length != 2){
            throw new IllegalArgumentException("Wrong number of Parameters for limit string expression");
        }

        final RecordList.Record currentRecord = recordList.getRecords().get(rowIndex);
        final String title = currentRecord.getValueByFieldName(params[0]);
        String pubMedId = currentRecord.getValueByFieldName(params[1]);

        if (pubMedId.isEmpty()){
            return title;
        }

        String result = "<a href=" + "http://www.ncbi.nlm.nih.gov/pubmed/"+pubMedId+" target=_blank>"+title+"</a>";
        return result;
    }

    public static String[] getParameters(FunctionType functionType, String function){
        //remove function type
        String regex = functionType.name();
        String funcParams = function.replaceAll(regex, "");
        return funcParams.split(",");
    }

    public enum FunctionType{
        CONCAT_DATE, ARITH, GET_YEAR, IF_NULL, LIMIT_STRING, PUBMED_URL;
    }
}
