package com.bookapp.noribook.Filter;

import android.widget.Filter;

import com.bookapp.noribook.Adapter.AdapterPdfAdmin;
import com.bookapp.noribook.Adapter.AdapterPdfUser;
import com.bookapp.noribook.Model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    ArrayList<ModelPdf> filterList ;

    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    // 필터링한 결과값 도출 filterlist(전체 검색대상) 사이즈만큼 i번 charSeq이 포함되어있는지 확인 후 포함되면 filteredModels array에 표시 (1개이상 존재), 아무것도 없으면 걍 전체 filterlist표시
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)) {
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
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;
        adapterPdfUser.notifyDataSetChanged();

    }
}