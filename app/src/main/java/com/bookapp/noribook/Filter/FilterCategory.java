package com.bookapp.noribook.Filter;

import android.widget.Filter;

import com.bookapp.noribook.Adapter.AdapterCategory;
import com.bookapp.noribook.Model.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    // 1. 찾을 filterlist와 필터가 추가될 어뎁터 선언
    ArrayList<ModelCategory> filterList;
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    // 필터링한 결과값 도출 filterlist(전체 검색대상) 사이즈만큼 i번 charSeq이 포함되어있는지 확인 후 포함되면 filteredModels array에 표시 (1개이상 존재), 아무것도 없으면 걍 전체 filterlist표시
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getCategory().toUpperCase().contains(charSequence)) {
                        filteredModels.add((filterList.get(i)));
                }
            }
            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;
        }else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }
        return filterResults;
    }
    // 3. 적용
    @Override
    protected void publishResults (CharSequence charSequence, FilterResults filterResults){
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)filterResults.values;
        adapterCategory.notifyDataSetChanged();

    }
}


