package org.wwarn.surveyor.client.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

/**
 * Added additional code
 */
public class RecordListCompressedWithInvertedIndexImpl extends RecordListCompressedImpl {
    InvertedIndex index;

    public RecordListCompressedWithInvertedIndexImpl(DataSchema schema) {
        super(schema);
        index = new InvertedIndex(schema);
    }

    public RecordListCompressedWithInvertedIndexImpl() {
        super();
    }

    @Override
    public void add(Record record) {
        super.add(record);
        index.addDocument(record.getFields());
    }

    @Override
    public void initialise() {
        super.initialise();

    }

    static class InvertedIndex implements IsSerializable{
        private final DataSchema schema;
        int docPosition=0;
        List<Map<String,Set<Integer>>> setList = new ArrayList<>(); // map of fields index to terms to document positions
        public InvertedIndex(DataSchema schema) {
            this.schema = schema;
            for (String field : schema.getColumns()) {
                final HashMap<String, Set<Integer>> e = new HashMap<String, Set<Integer>>();
                setList.add(e);
            }
        }

        InvertedIndex() {
            schema = null;
        }

        //add fields on index
        public void addDocument(String... fields){
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                final Map<String, Set<Integer>> stringSetMap = setList.get(i);
                Set<Integer> integers = stringSetMap.get(field);
                if(integers == null){
                    integers = new TreeSet<>();
                }
                integers.add(docPosition);
                stringSetMap.put(field, integers);
            }
            docPosition++;
        }

        public void initialise(){

        }
    }
}
