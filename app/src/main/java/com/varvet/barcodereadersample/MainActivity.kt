package com.varvet.barcodereadersample

import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.content.*
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.varvet.barcodereadersample.barcode.BarcodeCaptureActivity
import org.json.JSONObject
import javax.sql.CommonDataSource
import android.content.SharedPreferences
import com.varvet.barcodereadersample.utils.toJson
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore


class MainActivity : AppCompatActivity() {

    private val PREFS_CONTACTS = "prefs_contacts"
    private val KEY_CONTACTS = "contacts_list"
    private val ADD_CONTACT_REQUEST = 2

    private lateinit var mResultTextView: TextView
    private lateinit var mResultTextView2: TextView
    private lateinit var myContactsListView: ListView
    private lateinit var listViewAdapter:ArrayAdapter<String>


    val storeFileName = "securedStore";

    val keyPrefix = "vss";
//it's better to provide one, and you need to provide the same key each time after the first time
    val seedKey: ByteArray = "SecuredSeedData".toByteArray();

    companion object {
        const val QR_CODE_KEY = "pwr_krol"
        private val LOG_TAG = MainActivity::class.java.simpleName
        private val BARCODE_READER_REQUEST_CODE = 1
        private val ADD_NEW_CONTACT = 2

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     /*   setContentView(R.layout.activity_main)

        mResultTextView = findViewById(R.id.result_textview)
        editText = findViewById(R.id.TextBox);





        findViewById<Button>(R.id.scan_barcode_button).setOnClickListener {
            val intent = Intent(applicationContext, BarcodeCaptureActivity::class.java)
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
        }
        findViewById<Button>(R.id.generate_qr).setOnClickListener {
            val intent = Intent(applicationContext, Main2Activity::class.java)
            intent.putExtra("TextBox",editText.getText().toString());
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
        }
*/
        setContentView(R.layout.activity_main)
        SecuredPreferenceStore.init(getApplicationContext(), storeFileName, keyPrefix, seedKey, DefaultRecoveryHandler());


        myContactsListView = findViewById(R.id.myContactsListView)


        val string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string","[]")
        var contactsList = getArrayListFromJson(string)

        if(contactsList!=null) {
            var adapter = ContactAdapter(contactsList)
        }


        listViewAdapter = ArrayAdapter<String>(this,R.layout.row,contactsList)
        myContactsListView.adapter = listViewAdapter

        mResultTextView = findViewById(R.id.textView)
        mResultTextView2 = findViewById(R.id.textView2)

        findViewById<Button>(R.id.button).setOnClickListener{
            showMenuDialog()

        }
    }

    override fun onResume() {
        super.onResume()
        val string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string","[]")
        var contactsList = getArrayListFromJson(string)

        if(contactsList!=null) {
            var adapter = ContactAdapter(contactsList)
        }


        listViewAdapter = ArrayAdapter<String>(this,R.layout.row,contactsList)
        myContactsListView.adapter = listViewAdapter


    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
    super.onStop()
/*       //Save all contacts names
     val savedList = StringBuilder()
/*     for (contact in contactsList) {
         savedList.append(contact)
         savedList.append(",")
     }*/

     getSharedPreferences(PREFS_CONTACTS, Context.MODE_PRIVATE).edit()
             .putString(KEY_CONTACTS, savedList.toString()).apply()
*/
 }

override fun onDestroy() {
     super.onDestroy()
 }

     fun getArrayListFromJson(Json: String): ArrayList<String>? {
     if(Json.equals("{}")) return null
     val listType = TypeToken.getParameterized(ArrayList::class.java, String::class.java).type
     val x: ArrayList<String> = Gson().fromJson(Json, listType)
     return x
 }

    private fun showMenuDialog(){
     val pop = MenuDialogFragment()
     val fm = fragmentManager
     pop.show(fm,"name")
 }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

     if (requestCode == BARCODE_READER_REQUEST_CODE) {
             if (resultCode == CommonStatusCodes.SUCCESS) {
                 if (data != null) {
                     val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                     val p = barcode.cornerPoints


                     val intent = Intent(applicationContext, AddScanedContact::class.java)

                     intent.putExtra("newName",barcode.displayValue.toString());


                     startActivityForResult(intent, ADD_CONTACT_REQUEST)



                     mResultTextView.text = barcode.displayValue //<--- klucz przekazany
                     mResultTextView2.text = data.getStringExtra("message")

                 } else
                     mResultTextView.setText(R.string.no_barcode_captured)
             } else
                 Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                         CommonStatusCodes.getStatusCodeString(resultCode)))
         }
     else if (requestCode == ADD_CONTACT_REQUEST){
         if(resultCode == Activity.RESULT_OK){
             if(data!=null){
                 var string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string","[]")//dostep do SH
                 var contactsList = getArrayListFromJson(string)



                 if(contactsList!=null){
                     var adapter = ContactAdapter(contactsList)
                     val str = data.extras.getString("messenger")
                     val ou = str.split("<...>")
                    adapter.addItem(ou[0])

                     val prefStore = SecuredPreferenceStore.getSharedInstance()
                     prefStore.edit().putString(ou[0],ou[1]).apply()
                 }


//                 mResultTextView.text = ou[0]
  //               mResultTextView2.text = ou[1]


             }
         }

     }else
         super.onActivityResult(requestCode, resultCode, data)



 }

     private fun makeAdapter(list: List<String>): ArrayAdapter<String> =
         ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

     override fun onConfigurationChanged(newConfig: Configuration?) {
         super.onConfigurationChanged(newConfig)
        }

    inner class ContactAdapter(private val contactsList: ArrayList<String>):BaseAdapter(){

     override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
         val row = layoutInflater.inflate(R.layout.layout_main,parent, false)

         return row
     }

     fun addItem(name: String){
         contactsList.add(name)
         var sh = getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE)
         sh.edit().putString("string",contactsList.toJson()).apply()
     }
     override fun getItem(position: Int): Any {
        return contactsList.get(position) //To change body of created functions use File | Settings | File Templates.
     }

     override fun getItemId(position: Int): Long {
         return position.toLong()
     }

     override fun getCount(): Int {
         return contactsList.size //To change body of created functions use File | Settings | File Templates.
     }

 }


}



