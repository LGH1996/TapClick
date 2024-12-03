package com.lgh.tapclick.myactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lgh.tapclick.databinding.ActivityRegulationImportBinding;
import com.lgh.tapclick.databinding.ViewItemImportBinding;
import com.lgh.tapclick.mybean.AppDescribe;
import com.lgh.tapclick.mybean.Coordinate;
import com.lgh.tapclick.mybean.Regulation;
import com.lgh.tapclick.mybean.Widget;
import com.lgh.tapclick.myclass.DataDao;
import com.lgh.tapclick.myclass.MyApplication;
import com.lgh.tapclick.myfunction.MyUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RegulationImportActivity extends BaseActivity {
    public static List<Regulation> regulationList = new ArrayList<>();
    private final MyAdapter myAdapter = new MyAdapter();
    private final List<RegulationItem> regulationItemList = new ArrayList<>();
    private final List<RegulationItem> regulationItemFilterList = new ArrayList<>();
    private final List<Regulation> importList = new ArrayList<>();
    private ActivityRegulationImportBinding regulationImportBinding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        regulationImportBinding = ActivityRegulationImportBinding.inflate(getLayoutInflater());
        setContentView(regulationImportBinding.getRoot());
        regulationList.sort(new Comparator<Regulation>() {
            @Override
            public int compare(Regulation o1, Regulation o2) {
                return Collator.getInstance(Locale.CHINESE).compare(o1.appDescribe.appName, o2.appDescribe.appName);
            }
        });
        for (Regulation e : regulationList) {
            RegulationItem regulationItem = new RegulationItem(e);
            regulationItemList.add(regulationItem);
            regulationItemFilterList.add(regulationItem);
        }
        regulationImportBinding.recyclerView.setAdapter(myAdapter);

        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                regulationItemFilterList.clear();
                for (RegulationItem e : regulationItemList) {
                    String str = constraint.toString().toLowerCase();
                    if (e.regulation.appDescribe.appName.toLowerCase().contains(str)
                            || e.regulation.appDescribe.appPackage.contains(str)) {
                        regulationItemFilterList.add(e);
                    }
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                myAdapter.notifyDataSetChanged();
            }
        };
        regulationImportBinding.searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter.filter(s.toString().trim());
            }
        });

        regulationImportBinding.cbSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importList.clear();
                for (RegulationItem e : regulationItemList) {
                    e.isSelected = regulationImportBinding.cbSelectAll.isChecked();
                    boolean b = e.isSelected && importList.add(e.regulation);
                }
                myAdapter.notifyDataSetChanged();
                regulationImportBinding.tvSelectedNum.setText(String.format(Locale.ROOT, "已选%s项", importList.size()));
            }
        });

        regulationImportBinding.btImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (importList.isEmpty()) {
                    Toast.makeText(RegulationImportActivity.this, "请选择要导入的规则", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(RegulationImportActivity.this)
                        .setTitle("原有规则将被覆盖，确定导入？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<AppDescribe> appDescribes = new ArrayList<>();
                                List<Coordinate> coordinates = new ArrayList<>();
                                List<Widget> widgets = new ArrayList<>();
                                for (Regulation e : importList) {
                                    for (Coordinate coordinate : e.coordinateList) {
                                        coordinate.createTime = System.currentTimeMillis();
                                        coordinate.lastTriggerTime = 0;
                                        coordinate.triggerCount = 0;
                                    }
                                    for (Widget widget : e.widgetList) {
                                        widget.createTime = System.currentTimeMillis();
                                        widget.lastTriggerTime = 0;
                                        widget.triggerCount = 0;
                                        widget.triggerReason = "";
                                        widgets.add(widget);
                                    }
                                    appDescribes.add(e.appDescribe);
                                    coordinates.addAll(e.coordinateList);
                                    widgets.addAll(e.widgetList);
                                }
                                DataDao dataDao = MyApplication.dataDao;
                                dataDao.insertAppDescribes(appDescribes);
                                dataDao.insertCoordinates(coordinates);
                                dataDao.insertWidgets(widgets);
                                MyUtils.requestUpdateAllDate();
                                Toast.makeText(RegulationImportActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
            }
        });

        regulationImportBinding.recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(regulationImportBinding.searchBox.getText())) {
            regulationImportBinding.searchBox.setText(null);
            return;
        }
        super.onBackPressed();
    }

    static class RegulationItem {
        public Regulation regulation;
        public boolean isSelected;

        public RegulationItem(Regulation regulation) {
            this.regulation = regulation;
            this.isSelected = false;
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewItemImportBinding itemImportBinding = ViewItemImportBinding.inflate(getLayoutInflater(), parent, false);
            return new ViewHolder(itemImportBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RegulationItem regulationItem = regulationItemFilterList.get(position);
            holder.itemImportBinding.tvName.setText(String.format(Locale.ROOT, "%s (%s)",
                    regulationItem.regulation.appDescribe.appName,
                    regulationItem.regulation.appDescribe.appPackage));
            holder.itemImportBinding.tvDesc.setText(String.format(Locale.ROOT, "%d条坐标规则，%d条控件规则",
                    regulationItem.regulation.coordinateList.size(),
                    regulationItem.regulation.widgetList.size()));
            holder.itemImportBinding.cbSelect.setChecked(regulationItem.isSelected);
        }

        @Override
        public int getItemCount() {
            return regulationItemFilterList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewItemImportBinding itemImportBinding;

            public ViewHolder(ViewItemImportBinding binding) {
                super(binding.getRoot());
                itemImportBinding = binding;
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RegulationItem regulationItem = regulationItemFilterList.get(getAdapterPosition());
                        regulationItem.isSelected = !regulationItem.isSelected;
                        boolean b = regulationItem.isSelected ? importList.add(regulationItem.regulation) : importList.remove(regulationItem.regulation);
                        notifyItemChanged(getAdapterPosition());
                        regulationImportBinding.tvSelectedNum.setText(String.format(Locale.ROOT, "已选%s项", importList.size()));
                        regulationImportBinding.cbSelectAll.setChecked(importList.size() == RegulationImportActivity.this.regulationItemList.size());
                    }
                });
            }
        }
    }
}