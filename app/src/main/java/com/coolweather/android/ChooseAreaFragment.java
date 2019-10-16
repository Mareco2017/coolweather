package com.coolweather.android;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter; // 适配器
    private List<String> dataList = new ArrayList<>();  // 临时的集合

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载碎片的布局
        View view = inflater.inflate( R.layout.choose_area,container,false );

        // 获得布局中的控件对象
        titleText = view.findViewById( R.id.title_text );
        backButton = view.findViewById( R.id.back_button );
        listView = view.findViewById( R.id.list_view );

        // 创建一个适配器
        adapter = new ArrayAdapter<>( getContext(),android.R.layout.simple_list_item_1,dataList );

        // 为 ListView 设置适配器
        listView.setAdapter( adapter );

        // 返回设置好的 view
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated( savedInstanceState );

        // 为 ListView 子项的设置点击事件
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){ // 如果当前选中的级别是 省份
                    selectedProvince = provinceList.get( position ); // 从省份列表中获得已选择的省份
                    queryCities();// 去查询该省份下的城市
                } else if (currentLevel == LEVEL_CITY){ // 如果当前选中的级别是 城市
                    selectedCity = cityList.get( position );// 从城市列表中获得已选择的城市
                    queryCounties(); //去查询该城市下的县
                }
            }
        } );

        // 返回按钮的点击事件
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){ // 如果当前选中的是县
                    queryCities(); // 查询市级
                } else if (currentLevel == LEVEL_CITY){ // 如果当前选中的是城市
                    queryProvinces(); // 查询省份
                }
            }
        } );
        queryProvinces(); // 默认就是查询省份
    }


    /**
     * 查询全国所有的省份，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText( "中国" ); // 设置标题为中国
        backButton.setVisibility( View.GONE ); // 返回按钮不可见
        provinceList = LitePal.findAll(Province.class); // 查询本地的数据库

        if (provinceList.size() > 0){ //查询到了数据
            dataList.clear(); // 先清空集合内的内容
            for (Province province:provinceList){ // 遍历读取到的数据
                dataList.add( province.getProvinceName() ); // 取得省份名称，并添加到集合中
            }
            adapter.notifyDataSetChanged(); // 通知数据改变
            listView.setSelection( 0 ); // 设置默认选择第一项
            currentLevel = LEVEL_PROVINCE; // 设置当前选中的级别为 省份

        } else{ // 如果在本地数据库中，没有查询到数据--->去服务器上查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");    // √
        }
    }


    /**
     * 查询选中省内的城市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText( selectedProvince.getProvinceName() ); // 设置标题为省份名称
        backButton.setVisibility( View.VISIBLE ); // 设置返回按钮可见
        // 去本地数据库找 当前省份所对应的城市
        cityList = LitePal.where( "provinceid = ?",String.valueOf( selectedProvince.getId() ) ).find( City.class );

        if (cityList.size() > 0){ // 说明找到了
            dataList.clear();// 先把集合清空
            for (City city:cityList){
                dataList.add( city.getCityName() ); // 获取到城市名称，并添加到集合中
            }
            adapter.notifyDataSetChanged(); //通知数据改变
            listView.setSelection( 0 ); // 设置默认选择第一项
            currentLevel = LEVEL_CITY;

        } else { // 如果在本地数据库中，没有查询到数据--->去服务器上查询
            int provinceCode = selectedProvince.getProvinceCode(); // 获取到已选择的省份的代码
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }


    /**
     * 查询选中的市内的所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText( selectedCity.getCityName() );
        backButton.setVisibility( View.VISIBLE );
        countyList = LitePal.where( "cityid = ?",String.valueOf( selectedCity.getId() ) ).find( County.class );
        if (countyList.size() > 0){
            dataList.clear();
            for (County county:countyList){
                dataList.add( county.getCountyName() );
            }
            adapter.notifyDataSetChanged();
            listView.setSelection( 0 );
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+ provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }


    /**
     * 根据传入的地址和类型，从服务器上查询省市县数据
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog(); // 显示进度对话框    √
        HttpUtil.sendOkHttpRequest( address, new Callback() { // 调用方法，发送网络请求
            // 响应的数据会回调到 onResponse 方法中
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                // 对请求回来的数据，调用我们刚才写的解析处理的 Utility 类处理
                if ("province".equals( type )){ // 如果查询的类型是省份，那么解析请求回来的省份数据
                    result = Utility.handleProvinceResponse( responseText );
                } else if ("city".equals( type )){
                    result = Utility.handleCityResponse( responseText,selectedProvince.getId() );
                } else if ("county".equals( type )){
                    result = Utility.handleCountyResponse( responseText,selectedCity.getId() );
                }

                // 解析和处理完数据之后，再次调用查询省市县方法，来重新加载省市县数据
                if (result){
                    getActivity().runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals( type )){
                                queryProvinces();
                            } else if ("city".equals( type )){
                                queryCities();
                            } else if ("county".equals( type )){
                                queryCounties();
                            }
                        }
                    } );
                }
            }

            // 网络请求失败
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText( getContext(), "加载失败", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }

        } );
    }




    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog( getActivity() );
            progressDialog.setMessage( "正在加载..." );
            progressDialog.setCanceledOnTouchOutside( false );
        }
        progressDialog.show();
    }


    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }



}
