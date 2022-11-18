package com.lgh.advertising.going.myactivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lgh.advertising.going.R;
import com.lgh.advertising.going.databinding.ActivityEditDataBinding;
import com.lgh.advertising.going.databinding.ViewAutoFinderBinding;
import com.lgh.advertising.going.databinding.ViewBaseSettingBinding;
import com.lgh.advertising.going.databinding.ViewCoordinateBinding;
import com.lgh.advertising.going.databinding.ViewQuestionBinding;
import com.lgh.advertising.going.databinding.ViewWidgetBinding;
import com.lgh.advertising.going.mybean.AppDescribe;
import com.lgh.advertising.going.mybean.Coordinate;
import com.lgh.advertising.going.mybean.Widget;
import com.lgh.advertising.going.myclass.DataDao;
import com.lgh.advertising.going.myclass.MyApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditDataActivity extends BaseActivity {

    private AppDescribe appDescribe;
    private LayoutInflater inflater;
    private DataDao dataDao;
    private DisplayMetrics metrics;
    private SimpleDateFormat dateFormatModify;
    private SimpleDateFormat dateFormatCreate;
    private ActivityEditDataBinding editDataBinding;
    private ViewBaseSettingBinding baseSettingBinding;
    private ViewAutoFinderBinding autoFinderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editDataBinding = ActivityEditDataBinding.inflate(inflater = getLayoutInflater());
        setContentView(editDataBinding.getRoot());
        appDescribe = MyApplication.appDescribe;

        dataDao = MyApplication.dataDao;
        dateFormatModify = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        dateFormatCreate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault());
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        editDataBinding.rootLayout.setLayoutTransition(transition);
        editDataBinding.baseSettingLayout.setLayoutTransition(transition);
        editDataBinding.autoFinderLayout.setLayoutTransition(transition);
        editDataBinding.coordinateLayout.setLayoutTransition(transition);
        editDataBinding.widgetLayout.setLayoutTransition(transition);

        class QuestionClickListener implements View.OnClickListener {
            private final int strId;

            private QuestionClickListener(int strId) {
                this.strId = strId;
            }

            @Override
            public void onClick(View v) {
                ViewQuestionBinding questionBinding = ViewQuestionBinding.inflate(inflater);
                questionBinding.questionAnswer.setText(Html.fromHtml(getString(strId), Html.FROM_HTML_MODE_COMPACT));
                AlertDialog alertDialog = new AlertDialog.Builder(EditDataActivity.this).setView(questionBinding.getRoot()).setPositiveButton("确定", null).create();
                Window window = alertDialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.add_data_background);
                alertDialog.show();
            }
        }
        editDataBinding.baseSettingQuestion.setOnClickListener(new QuestionClickListener(R.string.baseSettingQuestion));
        editDataBinding.autoFinderQuestion.setOnClickListener(new QuestionClickListener(R.string.autoFinderQuestion));
        editDataBinding.coordinateQuestion.setOnClickListener(new QuestionClickListener(R.string.coordinateQuestion));
        editDataBinding.widgetQuestion.setOnClickListener(new QuestionClickListener(R.string.widgetQuestion));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (baseSettingBinding != null) {
            editDataBinding.baseSettingLayout.removeView(baseSettingBinding.getRoot());
        }
        baseSettingBinding = ViewBaseSettingBinding.inflate(inflater);
        baseSettingBinding.onOffName.setText(appDescribe.appName);
        baseSettingBinding.onOffSwitch.setChecked(appDescribe.onOff);

        baseSettingBinding.autoFinderSwitch.setChecked(appDescribe.autoFinderOnOFF);
        baseSettingBinding.autoFinderSustainTime.setText(String.valueOf(appDescribe.autoFinderRetrieveTime));
        baseSettingBinding.autoFinderRetrieveAllTime.setChecked(appDescribe.autoFinderRetrieveAllTime);

        baseSettingBinding.coordinateSwitch.setChecked(appDescribe.coordinateOnOff);
        baseSettingBinding.coordinateSustainTime.setText(String.valueOf(appDescribe.coordinateRetrieveTime));
        baseSettingBinding.coordinateRetrieveAllTime.setChecked(appDescribe.coordinateRetrieveAllTime);

        baseSettingBinding.widgetSwitch.setChecked(appDescribe.widgetOnOff);
        baseSettingBinding.widgetSustainTime.setText(String.valueOf(appDescribe.widgetRetrieveTime));
        baseSettingBinding.widgetRetrieveAllTime.setChecked(appDescribe.widgetRetrieveAllTime);

        Runnable baseSettingSaveRun = new Runnable() {
            @Override
            public void run() {
                String autoFinderTime = baseSettingBinding.autoFinderSustainTime.getText().toString();
                String coordinateTime = baseSettingBinding.coordinateSustainTime.getText().toString();
                String widgetTime = baseSettingBinding.widgetSustainTime.getText().toString();
                baseSettingBinding.baseSettingModify.setTextColor(0xffff0000);
                if (autoFinderTime.isEmpty() || coordinateTime.isEmpty() || widgetTime.isEmpty()) {
                    baseSettingBinding.baseSettingModify.setText("内容不能为空");
                    return;
                }
                appDescribe.onOff = baseSettingBinding.onOffSwitch.isChecked();
                appDescribe.autoFinderOnOFF = baseSettingBinding.autoFinderSwitch.isChecked();
                appDescribe.autoFinderRetrieveTime = Integer.parseInt(autoFinderTime);
                appDescribe.autoFinderRetrieveAllTime = baseSettingBinding.autoFinderRetrieveAllTime.isChecked();
                appDescribe.coordinateOnOff = baseSettingBinding.coordinateSwitch.isChecked();
                appDescribe.coordinateRetrieveTime = Integer.parseInt(coordinateTime);
                appDescribe.coordinateRetrieveAllTime = baseSettingBinding.coordinateRetrieveAllTime.isChecked();
                appDescribe.widgetOnOff = baseSettingBinding.widgetSwitch.isChecked();
                appDescribe.widgetRetrieveTime = Integer.parseInt(widgetTime);
                appDescribe.widgetRetrieveAllTime = baseSettingBinding.widgetRetrieveAllTime.isChecked();
                dataDao.updateAppDescribe(appDescribe);
                baseSettingBinding.baseSettingModify.setTextColor(0xff000000);
                baseSettingBinding.baseSettingModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        };
        baseSettingBinding.onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                baseSettingBinding.autoFinderSwitch.setChecked(isChecked);
                baseSettingBinding.widgetSwitch.setChecked(isChecked);
                baseSettingBinding.coordinateSwitch.setChecked(isChecked);
                baseSettingSaveRun.run();
            }
        });
        CompoundButton.OnCheckedChangeListener onOffCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                baseSettingSaveRun.run();
            }
        };
        baseSettingBinding.autoFinderSwitch.setOnCheckedChangeListener(onOffCheckedChangeListener);
        baseSettingBinding.widgetSwitch.setOnCheckedChangeListener(onOffCheckedChangeListener);
        baseSettingBinding.coordinateSwitch.setOnCheckedChangeListener(onOffCheckedChangeListener);

        TextWatcher sustainTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                baseSettingSaveRun.run();
            }
        };
        baseSettingBinding.autoFinderSustainTime.addTextChangedListener(sustainTimeTextWatcher);
        baseSettingBinding.widgetSustainTime.addTextChangedListener(sustainTimeTextWatcher);
        baseSettingBinding.coordinateSustainTime.addTextChangedListener(sustainTimeTextWatcher);

        CompoundButton.OnCheckedChangeListener allTimeCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                baseSettingSaveRun.run();
            }
        };
        baseSettingBinding.autoFinderRetrieveAllTime.setOnCheckedChangeListener(allTimeCheckedChangeListener);
        baseSettingBinding.widgetRetrieveAllTime.setOnCheckedChangeListener(allTimeCheckedChangeListener);
        baseSettingBinding.coordinateRetrieveAllTime.setOnCheckedChangeListener(allTimeCheckedChangeListener);
        editDataBinding.baseSettingLayout.addView(baseSettingBinding.getRoot());

        if (autoFinderBinding != null) {
            editDataBinding.autoFinderLayout.removeView(autoFinderBinding.getRoot());
        }
        autoFinderBinding = ViewAutoFinderBinding.inflate(inflater);
        autoFinderBinding.retrieveKeyword.setText(appDescribe.autoFinder.keywordList.isEmpty() ? "" : new Gson().toJson(appDescribe.autoFinder.keywordList));
        autoFinderBinding.retrieveNumber.setText(String.valueOf(appDescribe.autoFinder.retrieveNumber));
        autoFinderBinding.clickDelay.setText(String.valueOf(appDescribe.autoFinder.clickDelay));
        autoFinderBinding.clickOnly.setChecked(appDescribe.autoFinder.clickOnly);

        Runnable autoFinderSaveRun = new Runnable() {
            @Override
            public void run() {
                List<String> temKeyword;
                String keywordList = autoFinderBinding.retrieveKeyword.getText().toString().trim();
                String retrieveNumber = autoFinderBinding.retrieveNumber.getText().toString();
                String clickDelay = autoFinderBinding.clickDelay.getText().toString();
                autoFinderBinding.autoFinderModify.setTextColor(0xffff0000);
                try {
                    temKeyword = new Gson().fromJson(keywordList.isEmpty() || keywordList.equalsIgnoreCase("null") ? "[]" : keywordList, new TypeToken<List<String>>() {
                    }.getType());
                } catch (JsonSyntaxException jse) {
                    autoFinderBinding.autoFinderModify.setText("关键词格式填写错误");
                    return;
                }
                if (retrieveNumber.isEmpty() || clickDelay.isEmpty()) {
                    autoFinderBinding.autoFinderModify.setText("内容不能为空");
                    return;
                } else if (Integer.parseInt(retrieveNumber) < 1 || Integer.parseInt(retrieveNumber) > 100) {
                    autoFinderBinding.autoFinderModify.setText("检索次数应为１~100次之间");
                    return;
                } else if (Integer.parseInt(clickDelay) > 8000) {
                    autoFinderBinding.autoFinderModify.setText("点击延迟应为0~8000(ms)之间");
                    return;
                } else {
                    appDescribe.autoFinder.keywordList = temKeyword;
                    appDescribe.autoFinder.retrieveNumber = Integer.parseInt(retrieveNumber);
                    appDescribe.autoFinder.clickDelay = Integer.parseInt(clickDelay);
                    appDescribe.autoFinder.clickOnly = autoFinderBinding.clickOnly.isChecked();
                }
                dataDao.updateAutoFinder(appDescribe.autoFinder);
                autoFinderBinding.autoFinderModify.setTextColor(0xff000000);
                autoFinderBinding.autoFinderModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
            }
        };

        TextWatcher autoFinderTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                autoFinderSaveRun.run();
            }
        };
        autoFinderBinding.retrieveKeyword.addTextChangedListener(autoFinderTextWatcher);
        autoFinderBinding.retrieveNumber.addTextChangedListener(autoFinderTextWatcher);
        autoFinderBinding.clickDelay.addTextChangedListener(autoFinderTextWatcher);

        autoFinderBinding.clickOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoFinderSaveRun.run();
            }
        });
        editDataBinding.autoFinderLayout.addView(autoFinderBinding.getRoot());

        final List<Coordinate> coordinateList = new ArrayList<>(appDescribe.coordinateMap.values());
        if (coordinateList.isEmpty()) {
            editDataBinding.coordinateLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.coordinateLayout.setVisibility(View.VISIBLE);
        }
        coordinateList.sort(new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate o1, Coordinate o2) {
                return (int) (o2.createTime - o1.createTime);
            }
        });
        if (editDataBinding.coordinateLayout.getChildCount() > 2) {
            editDataBinding.coordinateLayout.removeViews(2, editDataBinding.coordinateLayout.getChildCount() - 2);
        }
        for (final Coordinate e : coordinateList) {
            final ViewCoordinateBinding coordinateBinding = ViewCoordinateBinding.inflate(inflater);
            coordinateBinding.coordinateActivity.setText(e.appActivity);
            coordinateBinding.coordinateXPosition.setText(String.valueOf(e.xPosition));
            coordinateBinding.coordinateYPosition.setText(String.valueOf(e.yPosition));
            coordinateBinding.coordinateClickDelay.setText(String.valueOf(e.clickDelay));
            coordinateBinding.coordinateClickInterval.setText(String.valueOf(e.clickInterval));
            coordinateBinding.coordinateClickNumber.setText(String.valueOf(e.clickNumber));
            coordinateBinding.coordinateComment.setText(e.comment);
            coordinateBinding.coordinateCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));

            Runnable coordinateSaveRun = new Runnable() {
                @Override
                public void run() {
                    String sX = coordinateBinding.coordinateXPosition.getText().toString();
                    String sY = coordinateBinding.coordinateYPosition.getText().toString();
                    String sDelay = coordinateBinding.coordinateClickDelay.getText().toString();
                    String sInterval = coordinateBinding.coordinateClickInterval.getText().toString();
                    String sNumber = coordinateBinding.coordinateClickNumber.getText().toString();
                    String sComment = coordinateBinding.coordinateComment.getText().toString().trim();
                    coordinateBinding.coordinateModify.setTextColor(0xffff0000);
                    if (sX.isEmpty() || sY.isEmpty() || sInterval.isEmpty() || sNumber.isEmpty()) {
                        coordinateBinding.coordinateModify.setText("内容不能为空");
                        return;
                    } else if (Integer.parseInt(sX) > metrics.widthPixels) {
                        coordinateBinding.coordinateModify.setText("X坐标超出屏幕寸");
                        return;
                    } else if (Integer.parseInt(sY) > metrics.heightPixels) {
                        coordinateBinding.coordinateModify.setText("Y坐标超出屏幕寸");
                        return;
                    } else if (Integer.parseInt(sDelay) > 8000) {
                        coordinateBinding.coordinateModify.setText("点击延迟应为0~8000(ms)之间");
                        return;
                    } else if (Integer.parseInt(sInterval) < 100 || Integer.parseInt(sInterval) > 2000) {
                        coordinateBinding.coordinateModify.setText("点击间隔应为100~2000(ms)之间");
                        return;
                    } else if (Integer.parseInt(sNumber) < 1 || Integer.parseInt(sNumber) > 20) {
                        coordinateBinding.coordinateModify.setText("点击次数应为1~20次之间");
                        return;
                    } else {
                        e.xPosition = Integer.parseInt(sX);
                        e.yPosition = Integer.parseInt(sY);
                        e.clickDelay = Integer.parseInt(sDelay);
                        e.clickInterval = Integer.parseInt(sInterval);
                        e.clickNumber = Integer.parseInt(sNumber);
                        e.comment = sComment;
                    }
                    dataDao.updateCoordinate(e);
                    coordinateBinding.coordinateModify.setTextColor(0xff000000);
                    coordinateBinding.coordinateModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                }
            };

            TextWatcher coordinateTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    coordinateSaveRun.run();
                }
            };

            coordinateBinding.coordinateXPosition.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateYPosition.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickDelay.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickInterval.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateClickNumber.addTextChangedListener(coordinateTextWatcher);
            coordinateBinding.coordinateComment.addTextChangedListener(coordinateTextWatcher);

            coordinateBinding.coordinateDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataDao.deleteCoordinate(e);
                    appDescribe.coordinateMap.remove(e.appActivity);
                    editDataBinding.coordinateLayout.removeView(coordinateBinding.getRoot());
                    if (appDescribe.coordinateMap.isEmpty()) {
                        editDataBinding.coordinateLayout.setVisibility(View.GONE);
                    }
                }
            });
            editDataBinding.coordinateLayout.addView(coordinateBinding.getRoot());
        }

        List<Set<Widget>> widgetSetList = new ArrayList<>(appDescribe.widgetSetMap.values());
        if (widgetSetList.isEmpty()) {
            editDataBinding.widgetLayout.setVisibility(View.GONE);
        } else {
            editDataBinding.widgetLayout.setVisibility(View.VISIBLE);
        }
        if (editDataBinding.widgetLayout.getChildCount() > 2) {
            editDataBinding.widgetLayout.removeViews(2, editDataBinding.widgetLayout.getChildCount() - 2);
        }
        for (final Set<Widget> widgetSet : widgetSetList) {
            final List<Widget> widgetList = new ArrayList<>(widgetSet);
            widgetList.sort(new Comparator<Widget>() {
                @Override
                public int compare(Widget o1, Widget o2) {
                    return (int) (o2.createTime - o1.createTime);
                }
            });
            for (final Widget e : widgetList) {
                final ViewWidgetBinding widgetBinding = ViewWidgetBinding.inflate(inflater);
                widgetBinding.widgetActivity.setText(e.appActivity);
                widgetBinding.widgetClickable.setText(String.valueOf(e.widgetClickable));
                widgetBinding.widgetRect.setText(e.widgetRect.toShortString());
                widgetBinding.widgetId.setText(e.widgetId);
                widgetBinding.widgetDescribe.setText(e.widgetDescribe);
                widgetBinding.widgetText.setText(e.widgetText);
                widgetBinding.widgetClickDelay.setText(String.valueOf(e.clickDelay));
                widgetBinding.widgetNoRepeat.setChecked(e.noRepeat);
                widgetBinding.widgetClickOnly.setChecked(e.clickOnly);
                widgetBinding.widgetComment.setText(e.comment);
                widgetBinding.widgetCreateTime.setText(dateFormatCreate.format(new Date(e.createTime)));

                Runnable widgetSaveRun = new Runnable() {
                    @Override
                    public void run() {
                        String clickDelay = widgetBinding.widgetClickDelay.getText().toString();
                        widgetBinding.widgetModify.setTextColor(0xffff0000);
                        if (clickDelay.isEmpty()) {
                            widgetBinding.widgetModify.setText("延迟点击不能为空");
                            return;
                        } else if (Integer.parseInt(clickDelay) > 8000) {
                            widgetBinding.widgetModify.setText("点击延迟应为0~8000(ms)之间");
                            return;
                        }
                        e.widgetId = widgetBinding.widgetId.getText().toString().trim();
                        e.widgetDescribe = widgetBinding.widgetDescribe.getText().toString().trim();
                        e.widgetText = widgetBinding.widgetText.getText().toString().trim();
                        e.clickDelay = Integer.parseInt(clickDelay);
                        e.noRepeat = widgetBinding.widgetNoRepeat.isChecked();
                        e.clickOnly = widgetBinding.widgetClickOnly.isChecked();
                        e.comment = widgetBinding.widgetComment.getText().toString().trim();
                        dataDao.updateWidget(e);
                        widgetBinding.widgetModify.setTextColor(0xff000000);
                        widgetBinding.widgetModify.setText(dateFormatModify.format(new Date()) + " (修改成功)");
                    }
                };

                TextWatcher widgetTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        widgetSaveRun.run();
                    }
                };

                widgetBinding.widgetId.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetDescribe.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetText.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetClickDelay.addTextChangedListener(widgetTextWatcher);
                widgetBinding.widgetComment.addTextChangedListener(widgetTextWatcher);

                CompoundButton.OnCheckedChangeListener widgetCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        widgetSaveRun.run();
                    }
                };
                widgetBinding.widgetNoRepeat.setOnCheckedChangeListener(widgetCheckedChangeListener);
                widgetBinding.widgetClickOnly.setOnCheckedChangeListener(widgetCheckedChangeListener);

                widgetBinding.widgetDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataDao.deleteWidget(e);
                        widgetSet.remove(e);
                        widgetList.remove(e);
                        editDataBinding.widgetLayout.removeView(widgetBinding.getRoot());
                        if (widgetSet.isEmpty()) {
                            appDescribe.widgetSetMap.remove(e.appActivity);
                        }
                        if (appDescribe.widgetSetMap.isEmpty()) {
                            editDataBinding.widgetLayout.setVisibility(View.GONE);
                        }
                    }
                });
                editDataBinding.widgetLayout.addView(widgetBinding.getRoot());
            }
        }
    }
}
