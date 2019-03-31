package org.by9steps.shadihall.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.by9steps.shadihall.AppController;
import org.by9steps.shadihall.R;
import org.by9steps.shadihall.adapters.BalSheetDateAdapter;
import org.by9steps.shadihall.helper.DatabaseHelper;
import org.by9steps.shadihall.model.BalSheet;
import org.by9steps.shadihall.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class DateBalSheetFragment extends Fragment {

    ProgressDialog mProgress;

    RecyclerView recyclerView;
    List<BalSheet> mList = new ArrayList<>();
    int m = 0, capital, profitLoss, liabilities, cpl, assets;
    int gCapital, gProfitLoss, gLiabilities, gCpl, gAssets;

    DatabaseHelper databaseHelper;
    List<BalSheet> balSheetList;


    public DateBalSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date_bal_sheet, container, false);
        recyclerView = view.findViewById(R.id.recycler);

        databaseHelper = new DatabaseHelper(getContext());

        List<User> list = User.listAll(User.class);
        for (User u : list){
            String query = "SELECT        CBDate, SUM(Capital) AS Capital, SUM(Expense) + SUM(Revenue) AS ProfitLoss, SUM(Liabilities) AS Liabilities, SUM(Expense) + SUM(Revenue) + SUM(Capital) + SUM(Liabilities) AS [C + P + L], SUM(Assets) AS Assets,\n" +
                    "                         ClientID\n" +
                    "FROM            (SELECT        derivedtbl_1.ClientID, derivedtbl_1.CBDate, Account1Type.AcTypeName, CAST(CASE WHEN AcTypeName = 'Expense' THEN (SUM(derivedtbl_1.Debit) - SUM(derivedtbl_1.Credit)) ELSE 0 END AS INT) AS Expense,\n" +
                    "                                                    CAST(CASE WHEN AcTypeName = 'Revenue' THEN (SUM(derivedtbl_1.Debit) - SUM(derivedtbl_1.Credit)) ELSE 0 END AS INT) AS Revenue, CAST(CASE WHEN AcTypeName = 'Capital' THEN (SUM(derivedtbl_1.Debit)\n" +
                    "                                                    - SUM(derivedtbl_1.Credit)) ELSE 0 END AS INT) AS Capital, CAST(CASE WHEN AcTypeName = 'Assets And Liability' THEN (CASE WHEN (SUM(Debit) - SUM(Credit)) > 0 THEN (SUM(Debit) - SUM(Credit)) ELSE 0 END)\n" +
                    "                                                    ELSE 0 END AS INT) AS Assets, CAST(CASE WHEN AcTypeName = 'Assets And Liability' THEN (CASE WHEN (SUM(Debit) - SUM(Credit)) < 0 THEN (SUM(Debit) - SUM(Credit)) ELSE 0 END) ELSE 0 END AS INT)\n" +
                    "                                                    AS Liabilities\n" +
                    "                          FROM            (SELECT        CreditAccount AS AccountID, 0 AS Debit, Amount AS Credit, ClientID, CBDate\n" +
                    "                                                    FROM            CashBook AS CashBook\n" +
                    "                                                    WHERE        (ClientID = "+u.getClientID()+")\n" +
                    "                                                    UNION ALL\n" +
                    "                                                    SELECT        DebitAccount AS AccountID, Amount AS Debit, 0 AS Credit, ClientID, CBDate\n" +
                    "                                                    FROM            CashBook AS CashBook_1\n" +
                    "                                                    WHERE        (ClientID = "+u.getClientID()+")) AS derivedtbl_1 INNER JOIN\n" +
                    "                                                    Account3Name ON derivedtbl_1.AccountID = Account3Name.AcNameID INNER JOIN\n" +
                    "                                                    Account2Group ON Account3Name.AcGroupID = Account2Group.AcGroupID INNER JOIN\n" +
                    "                                                    Account1Type ON Account2Group.AcTypeID = Account1Type.AcTypeID\n" +
                    "                          GROUP BY derivedtbl_1.ClientID, Account1Type.AcTypeName, derivedtbl_1.CBDate) AS derivedtbl_2\n" +
                    "GROUP BY ClientID, CBDate";
            balSheetList = databaseHelper.getBalSheet(query);
        }

        for (BalSheet b : balSheetList){
            String[] separated = b.getCBDate().split("-");
            if (m == 0) {
                mList.add(BalSheet.createSection(separated[1]+"/"+separated[2]));
                mList.add(BalSheet.createRow(b.getCBDate(),b.getCapital(),b.getProfitLoss(),b.getLiabilities(),b.getC_P_L(),b.getAssets(),b.getClientID()));
                m = Integer.valueOf(separated[1]);

                capital = Integer.valueOf(b.getCapital()) + capital;
                profitLoss = Integer.valueOf(b.getProfitLoss()) + profitLoss;
                liabilities = Integer.valueOf(b.getLiabilities()) + liabilities;
                cpl = Integer.valueOf(b.getC_P_L()) + cpl;
                assets = Integer.valueOf(b.getAssets()) + assets;
                gCapital = Integer.valueOf(b.getCapital()) + gCapital;
                gProfitLoss = Integer.valueOf(b.getProfitLoss()) + gProfitLoss;
                gLiabilities = Integer.valueOf(b.getLiabilities()) + gLiabilities;
                gCpl = Integer.valueOf(b.getC_P_L()) + gCpl;
                gAssets = Integer.valueOf(b.getAssets()) + gAssets;
            }else if (m == Integer.valueOf(separated[1])){
                capital = Integer.valueOf(b.getCapital()) + capital;
                profitLoss = Integer.valueOf(b.getProfitLoss()) + profitLoss;
                liabilities = Integer.valueOf(b.getLiabilities()) + liabilities;
                cpl = Integer.valueOf(b.getC_P_L()) + cpl;
                assets = Integer.valueOf(b.getAssets()) + assets;
                gCapital = Integer.valueOf(b.getCapital()) + gCapital;
                gProfitLoss = Integer.valueOf(b.getProfitLoss()) + gProfitLoss;
                gLiabilities = Integer.valueOf(b.getLiabilities()) + gLiabilities;
                gCpl = Integer.valueOf(b.getC_P_L()) + gCpl;
                gAssets = Integer.valueOf(b.getAssets()) + gAssets;
                mList.add(BalSheet.createRow(b.getCBDate(),b.getCapital(),b.getProfitLoss(),b.getLiabilities(),b.getC_P_L(),b.getAssets(),b.getClientID()));
            }else {
                mList.add(BalSheet.createTotal(String.valueOf(capital),String.valueOf(profitLoss),String.valueOf(liabilities),String.valueOf(cpl),String.valueOf(assets)));
                capital = 0; profitLoss = 0; liabilities = 0; cpl = 0; assets = 0;
                capital = Integer.valueOf(b.getCapital()) + capital;
                profitLoss = Integer.valueOf(b.getProfitLoss()) + profitLoss;
                liabilities = Integer.valueOf(b.getLiabilities()) + liabilities;
                cpl = Integer.valueOf(b.getC_P_L()) + cpl;
                assets = Integer.valueOf(b.getAssets()) + assets;
                gCapital = Integer.valueOf(b.getCapital()) + gCapital;
                gProfitLoss = Integer.valueOf(b.getProfitLoss()) + gProfitLoss;
                gLiabilities = Integer.valueOf(b.getLiabilities()) + gLiabilities;
                gCpl = Integer.valueOf(b.getC_P_L()) + gCpl;
                gAssets = Integer.valueOf(b.getAssets()) + gAssets;
                mList.add(BalSheet.createSection(separated[1]+"/"+separated[2]));
                mList.add(BalSheet.createRow(b.getCBDate(),b.getCapital(),b.getProfitLoss(),b.getLiabilities(),b.getC_P_L(),b.getAssets(),b.getClientID()));
                m = Integer.valueOf(separated[1]);
            }
        }
        mList.add(BalSheet.createTotal(String.valueOf(capital),String.valueOf(profitLoss),String.valueOf(liabilities),String.valueOf(cpl),String.valueOf(assets)));
        mList.add(BalSheet.createSection("Grand Total"));
        mList.add(BalSheet.createTotal(String.valueOf(gCapital),String.valueOf(gProfitLoss),String.valueOf(gLiabilities),String.valueOf(gCpl),String.valueOf(gAssets)));
        BalSheetDateAdapter adapter = new BalSheetDateAdapter(getContext(),mList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

//        getBalSheet();

        return view;
    }

    public void getBalSheet(){
        mProgress = new ProgressDialog(getContext());
        mProgress.setTitle("Loading");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/BalSheet.php";

        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.POST, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            JSONArray jsonArray = jsonObj.getJSONArray("BalSheet");
                            Log.e("SSSSS",jsonArray.toString());
                            String success = jsonObj.getString("success");
                            if (success.equals("1")){
//                                Account3Name.deleteAll(Account3Name.class);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("Recovery",jsonObject.toString());
                                    String CB_Date = jsonObject.getString("CBDate");
                                    JSONObject jbb = new JSONObject(CB_Date);
                                    String CBDate = jbb.getString("date");
                                    String Capital = jsonObject.getString("Capital");
                                    String ProfitLoss = jsonObject.getString("ProfitLoss");
                                    String Liabilities = jsonObject.getString("Liabilities");
                                    String C_P_L = jsonObject.getString("C + P + L");
                                    String Assets = jsonObject.getString("Assets");
                                    String ClientID = jsonObject.getString("ClientID");

                                    String pattern="yyyy-MM-dd";
                                    DateFormat df = new SimpleDateFormat(pattern);
                                    Date date = df.parse(CBDate);
                                    DateFormat dff = new SimpleDateFormat("dd-MM-yyyy");
                                    String cbDate = dff.format(date);
                                    String[] separated = cbDate.split("-");

                                    if (m == 0) {
                                        mList.add(BalSheet.createSection(separated[1]+"/"+separated[2]));
                                        mList.add(BalSheet.createRow(cbDate,Capital,ProfitLoss,Liabilities,C_P_L,Assets,ClientID));
                                        m = Integer.valueOf(separated[1]);

                                        capital = Integer.valueOf(Capital) + capital;
                                        profitLoss = Integer.valueOf(ProfitLoss) + profitLoss;
                                        liabilities = Integer.valueOf(Liabilities) + liabilities;
                                        cpl = Integer.valueOf(C_P_L) + cpl;
                                        assets = Integer.valueOf(Assets) + assets;
                                        gCapital = Integer.valueOf(Capital) + gCapital;
                                        gProfitLoss = Integer.valueOf(ProfitLoss) + gProfitLoss;
                                        gLiabilities = Integer.valueOf(Liabilities) + gLiabilities;
                                        gCpl = Integer.valueOf(C_P_L) + gCpl;
                                        gAssets = Integer.valueOf(Assets) + gAssets;
                                    }else if (m == Integer.valueOf(separated[1])){
                                        capital = Integer.valueOf(Capital) + capital;
                                        profitLoss = Integer.valueOf(ProfitLoss) + profitLoss;
                                        liabilities = Integer.valueOf(Liabilities) + liabilities;
                                        cpl = Integer.valueOf(C_P_L) + cpl;
                                        assets = Integer.valueOf(Assets) + assets;
                                        gCapital = Integer.valueOf(Capital) + gCapital;
                                        gProfitLoss = Integer.valueOf(ProfitLoss) + gProfitLoss;
                                        gLiabilities = Integer.valueOf(Liabilities) + gLiabilities;
                                        gCpl = Integer.valueOf(C_P_L) + gCpl;
                                        gAssets = Integer.valueOf(Assets) + gAssets;
                                        mList.add(BalSheet.createRow(cbDate,Capital,ProfitLoss,Liabilities,C_P_L,Assets,ClientID));
                                    }else {
                                        mList.add(BalSheet.createTotal(String.valueOf(capital),String.valueOf(profitLoss),String.valueOf(liabilities),String.valueOf(cpl),String.valueOf(assets)));
                                        capital = 0; profitLoss = 0; liabilities = 0; cpl = 0; assets = 0;
                                        capital = Integer.valueOf(Capital) + capital;
                                        profitLoss = Integer.valueOf(ProfitLoss) + profitLoss;
                                        liabilities = Integer.valueOf(Liabilities) + liabilities;
                                        cpl = Integer.valueOf(C_P_L) + cpl;
                                        assets = Integer.valueOf(Assets) + assets;
                                        gCapital = Integer.valueOf(Capital) + gCapital;
                                        gProfitLoss = Integer.valueOf(ProfitLoss) + gProfitLoss;
                                        gLiabilities = Integer.valueOf(Liabilities) + gLiabilities;
                                        gCpl = Integer.valueOf(C_P_L) + gCpl;
                                        gAssets = Integer.valueOf(Assets) + gAssets;
                                        mList.add(BalSheet.createSection(separated[1]+"/"+separated[2]));
                                        mList.add(BalSheet.createRow(cbDate,Capital,ProfitLoss,Liabilities,C_P_L,Assets,ClientID));
                                        m = Integer.valueOf(separated[1]);
                                    }
                                }
                                mList.add(BalSheet.createTotal(String.valueOf(capital),String.valueOf(profitLoss),String.valueOf(liabilities),String.valueOf(cpl),String.valueOf(assets)));
                                mList.add(BalSheet.createSection("Grand Total"));
                                mList.add(BalSheet.createTotal(String.valueOf(gCapital),String.valueOf(gProfitLoss),String.valueOf(gLiabilities),String.valueOf(gCpl),String.valueOf(gAssets)));
                                BalSheetDateAdapter adapter = new BalSheetDateAdapter(getContext(),mList);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(adapter);

                                mProgress.dismiss();

                            }else {
                                String message = jsonObj.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgress.dismiss();
                Log.e("Error",error.toString());
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                List<User> list = User.listAll(User.class);
                for (User u: list) {
                    params.put("ClientID", u.getClientID());
                    params.put("Date", "1");
                }
                return params;
            }
        };
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

}
