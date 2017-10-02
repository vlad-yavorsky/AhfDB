package ua.org.ahf.ahfdb.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ua.org.ahf.ahfdb.model.Company;
import ua.org.ahf.ahfdb.helper.DbHelper;
import ua.org.ahf.ahfdb.R;

public class UpdateFragment extends Fragment implements OnClickListener {

    private static final String JSON_URL = "http://ahf.org.ua/get-data.php";
    private static final String TAG_RESULT = "result";

    public UpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update, container, false);

        view.findViewById(R.id.updateDatabaseButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.updateDatabaseButton:
                getJSON();
                break;
        }
    }

    private void getJSON() {
        class GetJSON extends AsyncTask<String, Void, String> {
//            ProgressDialog loading;

//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                loading = ProgressDialog.show(UpdateFragment.this, "Please Wait...",null,true,true);
//            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(JSON_URL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);

                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        StringBuilder sb = new StringBuilder();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String json;
                        while ((json = bufferedReader.readLine()) != null) {
                            sb.append(json + "\n");
                        }
                        return sb.toString().trim();
                    }
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Update error: Time Out");
                    e.printStackTrace();
                } catch (java.io.IOException e) {
                    System.out.println("Update error: IOException");
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                super.onPostExecute(s);
                if (result != null) {
                    parseJSON(result);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                }
//                loading.dismiss();
            }
        }

        GetJSON gj = new GetJSON();
        gj.execute();
    }

    protected void parseJSON(String json){
        DbHelper.instance(getActivity()).deleteAllCompanies();

        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray companies = jsonObj.getJSONArray(TAG_RESULT);

            for(int i = 0; i < companies.length(); i++){
                JSONObject c = companies.getJSONObject(i);

                Long id = c.getLong(DbHelper.DbSchema.CompanyTable.Column.ID);
                Integer isMember = c.getInt(DbHelper.DbSchema.CompanyTable.Column.IS_MEMBER);
                Integer isHuntingGround = c.getInt(DbHelper.DbSchema.CompanyTable.Column.IS_HUNTING_GROUND);
                Integer isFishingGround = c.getInt(DbHelper.DbSchema.CompanyTable.Column.IS_FISHING_GROUND);
                Integer isPondFarm = c.getInt(DbHelper.DbSchema.CompanyTable.Column.IS_POND_FARM);
                String name = c.getString(DbHelper.DbSchema.CompanyTable.Column.NAME);
                String description = c.getString(DbHelper.DbSchema.CompanyTable.Column.DESCRIPTION);
                String website = c.getString(DbHelper.DbSchema.CompanyTable.Column.WEBSITE);
                String email = c.getString(DbHelper.DbSchema.CompanyTable.Column.EMAIL);
                String juridicalAddress = c.getString(DbHelper.DbSchema.CompanyTable.Column.JURIDICAL_ADDRESS);
                String actualAddress = c.getString(DbHelper.DbSchema.CompanyTable.Column.ACTUAL_ADDRESS);
                String director = c.getString(DbHelper.DbSchema.CompanyTable.Column.DIRECTOR);
                Integer isEnabled = c.getInt(DbHelper.DbSchema.CompanyTable.Column.IS_ENABLED);
                Integer oblastId = c.getInt(DbHelper.DbSchema.CompanyTable.Column.OBLAST_ID);
                String locale = c.getString(DbHelper.DbSchema.CompanyTable.Column.LOCALE);

                Double area = null;
                if(!c.isNull(DbHelper.DbSchema.CompanyTable.Column.AREA)) {
                    area = c.getDouble(DbHelper.DbSchema.CompanyTable.Column.AREA);
                }

                Double lat = null;
                if(!c.isNull(DbHelper.DbSchema.CompanyTable.Column.LAT)) {
                    lat = c.getDouble(DbHelper.DbSchema.CompanyTable.Column.LAT);
                }

                Double lng = null;
                if(!c.isNull(DbHelper.DbSchema.CompanyTable.Column.LNG)) {
                    lng = c.getDouble(DbHelper.DbSchema.CompanyTable.Column.LNG);
                }

                Company company = new Company(id, isMember, isHuntingGround, isFishingGround,
                        isPondFarm, area, lat, lng, name, description, website, email, juridicalAddress,
                        actualAddress, director, isEnabled, oblastId, locale);
                DbHelper.instance(getActivity()).create(company);
            }
            DbHelper.instance(getActivity()).createOblasts();
        } catch (JSONException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getActivity(), getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
    }
}