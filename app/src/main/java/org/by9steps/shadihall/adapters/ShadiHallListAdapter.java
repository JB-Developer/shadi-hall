package org.by9steps.shadihall.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.by9steps.shadihall.AppController;
import org.by9steps.shadihall.R;
import org.by9steps.shadihall.fragments.MenuItemsFragment;
import org.by9steps.shadihall.helper.DatabaseHelper;
import org.by9steps.shadihall.helper.Prefrence;
import org.by9steps.shadihall.model.Account1Type;
import org.by9steps.shadihall.model.Account2Group;
import org.by9steps.shadihall.model.Account3Name;
import org.by9steps.shadihall.model.ActiveClients;
import org.by9steps.shadihall.model.Bookings;
import org.by9steps.shadihall.model.CBUpdate;
import org.by9steps.shadihall.model.CashBook;
import org.by9steps.shadihall.model.Client;
import org.by9steps.shadihall.model.ProjectMenu;
import org.by9steps.shadihall.model.ShadiHallList;
import org.by9steps.shadihall.model.TableSession;
import org.by9steps.shadihall.model.UpdateDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShadiHallListAdapter extends RecyclerView.Adapter{

    Context mCtx;
    List<ActiveClients> mList;
    List<Client> clients;

    private ProgressDialog mProgress;
    Prefrence prefrence;

    //shared prefrences
    SharedPreferences sharedPreferences;
    public static final String mypreference = "mypref";
    public static final String log_in = "loginKey";
    public static final String phone = "phoneKey";

    DatabaseHelper databaseHelper;

    String cId, ph;

    String clientID, clientUserID, userRights, projectID;

    private OnItemClickListener listener;

    public ShadiHallListAdapter(Context mCtx, List<ActiveClients> mList) {
        this.mCtx = mCtx;
        this.mList = mList;
        prefrence = new Prefrence(mCtx);
        databaseHelper = new DatabaseHelper(mCtx);
        //shared prefrences
        sharedPreferences = mCtx.getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.shadihall_list_item, null);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final ActiveClients shadiHallList = mList.get(position);

        ((ListViewHolder)viewHolder).name.setText(shadiHallList.getCompanyName());
        ((ListViewHolder)viewHolder).address.setText(shadiHallList.getCompanyAddress());
        ((ListViewHolder)viewHolder).rights.setText(shadiHallList.getUserRights());

        Picasso.get()
                .load(AppController.imageUrl+shadiHallList.getClientID()+"/logo.png")
                .placeholder(R.drawable.default_avatar)
                .into(((ListViewHolder)viewHolder).image);

        ((ListViewHolder)viewHolder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = "SELECT * FROM Client WHERE ClientID = "+ shadiHallList.getClientID();
                clients = databaseHelper.getClient(query);

                if (clients.size() > 0){
//                    User.deleteAll(User.class);
//                    for (Client c : clients) {
//                        User user = new User(c.getClientID(), "0", "0", "0", "0", c.getClientUserID(), "ss");
//                        user.save();
//                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(log_in, "Yes");
                    editor.apply();

                    if (isConnected()) {
                        clientID = shadiHallList.getClientID();
                        clientUserID = shadiHallList.getClientUserID();
                        userRights = shadiHallList.getUserRights();
                        projectID = shadiHallList.getProjectID();
                        mProgress = new ProgressDialog(mCtx);
                        mProgress.setTitle("Getting Details");
                        mProgress.setMessage("Please wait...");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        getCashBook();
                    }else {
                        if (listener != null)
                            listener.onItemClick(shadiHallList.getClientID(),shadiHallList.getClientUserID(), shadiHallList.getProjectID(), shadiHallList.getUserRights());
                    }
                }else {

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mCtx);
                    View mView = layoutInflaterAndroid.inflate(R.layout.password_input_dialog_box, null);
                    AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mCtx);
                    alertDialogBuilderUserInput.setView(mView);

                    final EditText password = (EditText) mView.findViewById(R.id.userInputDialog);
                    alertDialogBuilderUserInput
                            .setCancelable(false)
                            .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    Log.e("Password",password.getText().toString());
                                    if (!password.getText().toString().equals("")) {
                                        clientID = shadiHallList.getClientID();
                                        clientUserID = shadiHallList.getClientUserID();
                                        userRights = shadiHallList.getUserRights();
                                        projectID = shadiHallList.getProjectID();

                                        login(shadiHallList.getClientID(), shadiHallList.getClientUserID(), password.getText().toString());
                                    }else {
                                        Toast.makeText(mCtx, "Enter Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })

                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                        }
                                    });

                    AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                    alertDialogAndroid.show();

                }
            }

        });


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView name, address, rights;

        public ListViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            rights = itemView.findViewById(R.id.user_rights);

        }
    }

    //load fragment on recyclerView OnClickListner
    public  void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String clientID, String clientUserId, String projectID, String userRights);
    }

    //Send Request To Server
    private void login(final String mClientID, final String mClientUserID, final String mPassword){

        if(sharedPreferences.contains(phone)){
            ph = sharedPreferences.getString(phone,"");
        }

        mProgress = new ProgressDialog(mCtx);
        mProgress.setTitle("Checking credentials");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

//        CBUpdate.deleteAll(CBUpdate.class);

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/Login.php";

        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.POST, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RES",response);
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            String success = jsonObj.getString("success");
                            String message = jsonObj.getString("message");
                            if (success.equals("1")){
                                JSONArray jsonArray = jsonObj.getJSONArray("UserInfo");
                                Log.e("UserInfo",jsonArray.toString());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String cashID = jsonObject.getString("CashID");
                                    String bookingIncomeID = jsonObject.getString("BookingIncomeID");
                                    String bookingExpenseID = jsonObject.getString("BookingExpenseID");
                                    String acNameID = "0";
                                    String acName = "ss";
                                    cId = jsonObject.getString("ClientID");
                                    String ClientParentID = jsonObject.getString("ClientParentID");
                                    String EntryType = jsonObject.getString("EntryType");
                                    String LoginMobileNo = jsonObject.getString("LoginMobileNo");
                                    String CompanyName = jsonObject.getString("CompanyName");
                                    String CompanyAddress = jsonObject.getString("CompanyAddress");
                                    String CompanyNumber = jsonObject.getString("CompanyNumber");
                                    String NameOfPerson = jsonObject.getString("NameOfPerson");
                                    String Email = jsonObject.getString("Email");
                                    String WebSite = jsonObject.getString("WebSite");
                                    String Password = jsonObject.getString("Password");
                                    String ActiveClient = jsonObject.getString("ActiveClient");
                                    String Country = jsonObject.getString("Country");
                                    String City = jsonObject.getString("City");
                                    String SubCity = jsonObject.getString("SubCity");
                                    String CapacityOfPersons = jsonObject.getString("CapacityOfPersons");
                                    String ClientUserID = jsonObject.getString("ClientUserID");
                                    String SysCode = jsonObject.getString("SysCode");
                                    String NetCode = jsonObject.getString("NetCode");
                                    String up = jsonObject.getString("UpdatedDate");
                                    JSONObject jb = new JSONObject(up);
                                    String UpdatedDate = jb.getString("date");
                                    String Lat = jsonObject.getString("Lat");
                                    String Lng = jsonObject.getString("Lng");
                                    String ProjectID = jsonObject.getString("ProjectID");

                                    databaseHelper.createClient(new Client(cId, ClientParentID, EntryType, LoginMobileNo, CompanyName,
                                            CompanyAddress, CompanyNumber, NameOfPerson, Email, WebSite, Password, ActiveClient,
                                            Country, City, SubCity, CapacityOfPersons, ClientUserID, SysCode, NetCode, UpdatedDate,
                                            Lat, Lng, ProjectID));

//                                    User user = new User(cId,cashID,bookingIncomeID,bookingExpenseID,acNameID,ClientUserID,acName);
//                                    user.save();
                                }

                                if (isConnected()) {
                                    getCashBook();
                                }else {
                                    Toast.makeText(mCtx, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                mProgress.dismiss();
                                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgress.dismiss();
                Log.e("Error",error.toString());
                Toast.makeText(mCtx, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ClientID", mClientID);
                params.put("ClientUserID", mClientUserID);
                params.put("Password", mPassword);
                return params;
            }
        };
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getCashBook(){

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/Test2.php";

        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.POST, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            JSONArray jsonArray = jsonObj.getJSONArray("CashBook");
                            String success = jsonObj.getString("success");
                            if (success.equals("1")){
                                databaseHelper.deleteCashBook();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("CashBook",jsonObject.toString());
                                    String CashBookID = jsonObject.getString("CashBookID");
                                    String cb = jsonObject.getString("CBDate");
                                    JSONObject jbb = new JSONObject(cb);
                                    String CBDate = jbb.getString("date");
                                    SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = ss.parse(CBDate);
                                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                                    String CBDate1 = sf.format(date);
                                    String DebitAccount = jsonObject.getString("DebitAccount");
                                    String CreditAccount = jsonObject.getString("CreditAccount");
                                    String CBRemark = jsonObject.getString("CBRemarks");
                                    String Amount = jsonObject.getString("Amount");
                                    String ClientID = jsonObject.getString("ClientID");
                                    String ClientUserID = jsonObject.getString("ClientUserID");
                                    String NetCode = jsonObject.getString("NetCode");
                                    String SysCode = jsonObject.getString("SysCode");
                                    String UpdatedDate = jsonObject.getString("UpdatedDate");
//                                    JSONObject jb = new JSONObject(ed);
//                                    String UpdatedDate = jb.getString("date");
                                    String TableID = jsonObject.getString("TableID");
                                    String SessionDate = jsonObject.getString("SessionDate");
                                    String SerialNo = jsonObject.getString("SerialNo");
                                    String TableName = jsonObject.getString("TableName");

                                    databaseHelper.createCashBook(new CashBook(CashBookID,CBDate1,DebitAccount,CreditAccount,CBRemark,Amount,ClientID,ClientUserID,NetCode,SysCode,UpdatedDate,TableID, SerialNo, TableName));

                                }
//                                mProgress.dismiss();

                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            getAccount3Name();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mProgress.dismiss();
                Log.e("Error",error.toString());
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ClientID", clientID);
                return params;
            }
        };
        int socketTimeout = 10000;//10 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getAccount3Name(){

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/test.php";

        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.POST, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            JSONArray jsonArray = jsonObj.getJSONArray("Account3Name");
                            String success = jsonObj.getString("success");
                            if (success.equals("1")){
                                databaseHelper.deleteAccount3Name();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("Account3Name",jsonObject.toString());
                                    String AcNameID = jsonObject.getString("AcNameID");
                                    String AcName = jsonObject.getString("AcName");
                                    String AcGroupID = jsonObject.getString("AcGroupID");
                                    String AcAddress = jsonObject.getString("AcAddress");
                                    String AcMobileNo = jsonObject.getString("AcMobileNo");
                                    String AcContactNo = jsonObject.getString("AcContactNo");
                                    String AcEmailAddress = jsonObject.getString("AcEmailAddress");
                                    String AcDebitBal = jsonObject.getString("AcDebitBal");
                                    String AcCreditBal = jsonObject.getString("AcCreditBal");
                                    String AcPassward = jsonObject.getString("AcPassward");
                                    String ClientID = jsonObject.getString("ClientID");
                                    String ClientUserID = jsonObject.getString("ClientUserID");
                                    String SysCode = jsonObject.getString("SysCode");
                                    String NetCode = jsonObject.getString("NetCode");
                                    String ed = jsonObject.getString("UpdatedDate");
                                    JSONObject jbb = new JSONObject(ed);
                                    String UpdatedDate = jbb.getString("date");
                                    String SerialNo = jsonObject.getString("SerialNo");
                                    String UserRights = jsonObject.getString("UserRights");
                                    String SecurityRights = jsonObject.getString("SecurityRights");
                                    String Salary = jsonObject.getString("Salary");
                                    String SessionDate = jsonObject.getString("SessionDate");

                                    databaseHelper.createAccount3Name(new Account3Name(AcNameID,AcName,AcGroupID,AcAddress,AcMobileNo,AcContactNo,AcEmailAddress,AcDebitBal,AcCreditBal,AcPassward,ClientID,ClientUserID,SysCode,NetCode,UpdatedDate,SerialNo,UserRights,SecurityRights,Salary));

//                                    if (i == jsonArray.length() - 1) {
//                                        TableSession.deleteAll(TableSession.class);
//                                        TableSession session = new TableSession("Account3Name", AcNameID, SessionDate, SessionDate);
//                                        session.save();
//                                    }
                                }
                                getAccountGroups();

                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mProgress.dismiss();
                Log.e("Error",error.toString());
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ClientID", clientID);
                return params;
            }
        };
        int socketTimeout = 10000;//10 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getAccountGroups(){

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetAccountGroup.php";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RES",response);
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            JSONArray jsonArray = jsonObj.getJSONArray("Account2Group");
                            String success = jsonObj.getString("success");
                            Log.e("Success",success);
                            if (success.equals("1")){
                                databaseHelper.deleteAccount2Group();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("Recovery",jsonObject.toString());
                                    String AcGroupID = jsonObject.getString("AcGroupID");
                                    String AcTypeID = jsonObject.getString("AcTypeID");
                                    String AcGruopName = jsonObject.getString("AcGruopName");

                                    databaseHelper.createAccount2Group(new Account2Group(AcGroupID,AcTypeID,AcGruopName));
                                }

                                getAccountTypes();
//
//                                mProgress.dismiss();
//                                getCashBook();

                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mProgress.dismiss();
                Log.e("Error",error.toString());
//                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getAccountTypes(){

        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetAccountType.php";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Account1Type",response);
//                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            JSONArray jsonArray = jsonObj.getJSONArray("Account1Type");
                            String success = jsonObj.getString("success");
                            Log.e("Success",success);
                            if (success.equals("1")){
                                databaseHelper.deleteAccount1Type();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("Recovery",jsonObject.toString());
                                    String AcTypeID = jsonObject.getString("AcTypeID");
                                    String AcTypeName = jsonObject.getString("AcTypeName");

                                    databaseHelper.createAccount1Type(new Account1Type(AcTypeID,AcTypeName));
                                }
                                getItem1Type();
                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error",error.toString());
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getItem1Type(){
        Log.e("GetItem1Type","OK");
        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetItem1Type.php";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("GetItem1Type",response);
//                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            String success = jsonObj.getString("success");
                            Log.e("Success",success);
                            if (success.equals("1")){
                                JSONArray jsonArray = jsonObj.getJSONArray("Item1Type");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("GetItem1Type",jsonObject.toString());
                                    String Item1TypeID = jsonObject.getString("Item1TypeID");
                                    String ItemType = jsonObject.getString("ItemType");

//                                    databaseHelper.createAccount1Type(new Account1Type(AcTypeID,AcTypeName));
                                }
                                getProjectMenu();
                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error",error.toString());
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getProjectMenu(){
        Log.e("GetProjectMenu","OK");
        String tag_json_obj = "json_obj_req";
        String u = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetProjectMenu.php";

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("GetProjectMenu",response);
//                        mProgress.dismiss();
                        JSONObject jsonObj = null;

                        try {
                            jsonObj= new JSONObject(response);
                            String success = jsonObj.getString("success");
                            Log.e("ProjectMenu",success);
                            if (success.equals("1")){
                                JSONArray jsonArray = jsonObj.getJSONArray("ProjectMenu");
                                databaseHelper.deleteProjectMenu();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e("ProjectMenu",jsonObject.toString());
                                    String MenuID = jsonObject.getString("MenuID");
                                    String ProjectID = jsonObject.getString("ProjectID");
                                    String MenuGroup = jsonObject.getString("MenuGroup");
                                    String MenuName = jsonObject.getString("MenuName");
                                    String PageOpen = jsonObject.getString("PageOpen");
                                    String ValuePass = jsonObject.getString("ValuePass");
                                    String ImageName = jsonObject.getString("ImageName");
                                    String GroupSortBy = jsonObject.getString("GroupSortBy");
                                    String SortBy = jsonObject.getString("SortBy");

                                    databaseHelper.createProjectMenu(new ProjectMenu(MenuID, ProjectID, MenuGroup, MenuName, PageOpen, ValuePass, ImageName, GroupSortBy,SortBy));
                                }

                                getBookings();

                            }else {
                                String message = jsonObj.getString("message");
//                                Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error",error.toString());
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getBookings(){
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";
        String url = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetBookings.php";


        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String text = "", BookingID = "", ClientName = "", ClientMobile = "", ClientAddress = "", ClientNic = "", EventName = "", BookingDate = "", EventDate = "",
                                ArrangePersons ="", ChargesTotal = "",Description = "", ClientID ="", ClientUserID = "", NetCode = "",SysCode = "", UpdatedDate = "";
                        try {
                            JSONObject json = new JSONObject(response);
                            String success = json.getString("success");
                            Log.e("Response",success);

                            if (success.equals("1")) {
                                JSONArray jsonArray = json.getJSONArray("Bookings");
                                Log.e("SSSS", jsonArray.toString());
                                databaseHelper.deleteBooking();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    BookingID = jsonObject.getString("BookingID");
                                    ClientName = jsonObject.getString("ClientName");
                                    ClientMobile = jsonObject.getString("ClientMobile");
                                    ClientAddress = jsonObject.getString("ClientAddress");
                                    ClientNic = jsonObject.getString("ClientNic");
                                    EventName = jsonObject.getString("EventName");
                                    String bd = jsonObject.getString("BookingDate");
                                    JSONObject jbbb = new JSONObject(bd);
                                    BookingDate = jbbb.getString("date");
                                    String ed = jsonObject.getString("EventDate");
                                    JSONObject jb = new JSONObject(ed);
                                    EventDate = jb.getString("date");
                                    Log.e("TEST",EventDate);
                                    ArrangePersons = jsonObject.getString("ArrangePersons");
                                    ChargesTotal = jsonObject.getString("ChargesTotal");
                                    Description = jsonObject.getString("Description");
                                    ClientID = jsonObject.getString("ClientID");
                                    ClientUserID = jsonObject.getString("ClientUserID");
                                    NetCode = jsonObject.getString("NetCode");
                                    SysCode = jsonObject.getString("SysCode");
                                    Log.e("TEST","TEST");
                                    String up = jsonObject.getString("UpdatedDate");
                                    JSONObject jbb = new JSONObject(up);
                                    UpdatedDate = jbb.getString("date");
                                    String SessionDate = jsonObject.getString("SessionDate");
                                    String Shift = jsonObject.getString("Shift");
                                    String SerialNo = jsonObject.getString("SerialNo");

                                    Log.e("TEST","SSSS");

                                    databaseHelper.createBooking(new Bookings(BookingID,ClientName,ClientMobile,ClientAddress,ClientNic,EventName,BookingDate,EventDate,ArrangePersons,ChargesTotal,Description,ClientID,ClientUserID,NetCode,SysCode,UpdatedDate,Shift, SerialNo));

//                                    if (i == jsonArray.length() - 1) {
//                                        TableSession session = new TableSession("Bookings", BookingID, SessionDate, SessionDate);
//                                        session.save();
//                                    }
                                }
                                getAccount4UserRights();

//                                FetchFromDb();
//                                pDialog.dismiss();
                            }else {
//                                pDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
//                pDialog.dismiss();
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ClientID", clientID);
                return params;
            }
        };

        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    public void getAccount4UserRights(){
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";
        String url = "http://69.167.137.121/plesk-site-preview/sky.com.pk/shadiHall/GetAccount4UserRights.php";


        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject json = new JSONObject(response);
                            String success = json.getString("success");
                            Log.e("Success",success);

                            if (success.equals("1")) {
                                JSONArray jsonArray = json.getJSONArray("Account4UserRights");
                                Log.e("Account4UserRights", jsonArray.toString());
//                                databaseHelper.deleteBooking();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    String AcRightsID = jsonObject.getString("AcRightsID");
                                    String Account3ID = jsonObject.getString("Account3ID");
                                    String MenuName = jsonObject.getString("MenuName");
                                    String PageOpen = jsonObject.getString("PageOpen");
                                    String ValuePass = jsonObject.getString("ValuePass");
                                    String ImageName = jsonObject.getString("ImageName");
                                    String Inserting = jsonObject.getString("Inserting");
                                    String Edting = jsonObject.getString("Edting");
                                    String Deleting = jsonObject.getString("Deleting");
                                    String Reporting = jsonObject.getString("Reporting");
                                    String ClientID = jsonObject.getString("ClientID");
                                    String ClientUserID = jsonObject.getString("ClientUserID");
                                    String NetCode = jsonObject.getString("NetCode");
                                    String SysCode = jsonObject.getString("SysCode");
                                    String UpdateDate = jsonObject.getString("UpdateDate");
                                    String SortBy = jsonObject.getString("SortBy");

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(log_in, "Yes");
                                    editor.apply();

                                    if (listener != null)
                                        listener.onItemClick(clientID,clientUserID, projectID, userRights);

                                    mProgress.dismiss();

                                }
                            }else {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(log_in, "Yes");
                                editor.apply();

                                if (listener != null)
                                    listener.onItemClick(clientID,clientUserID, projectID, userRights);

                                mProgress.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
//                pDialog.dismiss();
//                Toast.makeText(SplashActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Account3ID", "1");
                return params;
            }
        };

        int socketTimeout = 10000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }

    //Check Internet Connection
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}
