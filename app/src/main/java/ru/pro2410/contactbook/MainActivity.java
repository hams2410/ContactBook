package ru.pro2410.contactbook;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DrawerLayout lDrawer;
    boolean isEditTextTouch;

    String [] arraySlideMenu;
    ListView listViewSlideMenu;
    ListView listViewContact;
    EditText editTextCopy;
    ArrayList<ContactModel> arrayListContactAll = new ArrayList<>();
    ArrayList<ContactModel> arrayListCurrent = arrayListContactAll;
    ArrayAdapter<String> adapterMenu;
    ContactAdapter contactAdapter;
    int currentIdGroup = 0;
    View selectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lDrawer = (DrawerLayout) findViewById(R.id.idDrawer);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setCustomView(R.layout.contact_action_bar);

        final EditText editTextSearch = (EditText) actionBar.getCustomView().findViewById(R.id.idEditTextSearch);
        editTextCopy = editTextSearch;

        ImageButton imageButtonMenu = (ImageButton) actionBar.getCustomView().findViewById(R.id.idMenuButton);

        editTextSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (lDrawer.isDrawerOpen(GravityCompat.START)) lDrawer.closeDrawer(GravityCompat.START);
                isEditTextTouch = true;
                Log.d("TAG", String.valueOf(editTextSearch.hasFocus()+" setOntouch"));
                return false;
            }
        });

        lDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }
            @Override
            public void onDrawerClosed(View drawerView) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        imageButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (lDrawer.isDrawerOpen(GravityCompat.START)) lDrawer.closeDrawer(GravityCompat.START);
            else lDrawer.openDrawer(GravityCompat.START);
            }
        });

        imageButtonMenu.setBackgroundColor(Color.TRANSPARENT);

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Toast.makeText(MainActivity.this,"Edit "+editTextSearch.getText().toString(),Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        lDrawer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("TAG", String.valueOf(editTextSearch.hasFocus()+" onGlobal"));
                if (!editTextSearch.hasFocus()) {
                    int heightDiff = lDrawer.getRootView().getHeight() - lDrawer.getHeight();
                    if (heightDiff > dpToPx(200)) { // if more than 200 dp, it's probably a keyboard...
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }else if (lDrawer.isDrawerOpen(GravityCompat.START)){
                    isEditTextTouch = false;
                }

                if (selectView!=null)selectView.setSelected(true);

            }
        });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);

        arraySlideMenu = getResources().getStringArray(R.array.slide_menu_list);
        listViewSlideMenu = (ListView) findViewById(R.id.left_drawer);
        adapterMenu = new MenuAdapter(this,
                R.layout.textview_slidemenu, arraySlideMenu);
        listViewSlideMenu.setAdapter(adapterMenu);

        listViewSlideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                if (position==arraySlideMenu.length-1){
                    finish();
                    return;
                }
                selectView = view;

                currentIdGroup = position;
                arrayListCurrent = getFilteredListContact();
                contactAdapter.arrayListDisplayed = arrayListCurrent;
                contactAdapter.getFilter().filter(editTextSearch.getText().toString());
                lDrawer.closeDrawer(GravityCompat.START);
            }
        });

        listViewContact = (ListView) findViewById(R.id.list_Contact);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactAdapter.getFilter().filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        inicializeArray();
    }

    class MenuAdapter extends ArrayAdapter<String>{
        LayoutInflater inflater;
        String [] str;


        public MenuAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
            str = objects;
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)convertView = inflater.inflate(R.layout.textview_slidemenu,null);
            TextView textView = (TextView) convertView.findViewById(R.id.idTextSlideMenu);
            textView.setText(str[position]);
            if (position==0) selectView = convertView;
            return convertView;
        }
    }

    public float dpToPx(float valueInDp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }


    private void createDialogs(int idDialog, final ContactModel contactModel) {
        final Dialog dialog = new Dialog(this);
        switch (idDialog){
            case R.id.idDialogContactInfo:

                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.create_contact_dialog_layout,null);
                dialog.setContentView(view);
                dialog.setTitle(R.string.contactInfo);
                TextView textView = (TextView) view.findViewById(R.id.idTextName);
                textView.setText(contactModel.name);
                textView = (TextView) view.findViewById(R.id.idTextAddress);
                textView.setText(contactModel.address);
                textView = (TextView) view.findViewById(R.id.idTextTel);
                textView.setText(contactModel.tel);
                CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.idCheckBox1);
                setInitialChecked(contactModel,1,checkBox1);

                checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                       setChecked(contactModel,1,isChecked);
                    }
                });
                CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.idCheckBox2);
                setInitialChecked(contactModel,2,checkBox2);

                checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setChecked(contactModel,2,isChecked);
                    }
                });
                CheckBox checkBox3 = (CheckBox) view.findViewById(R.id.idCheckBox3);
                setInitialChecked(contactModel,3,checkBox3);

                checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setChecked(contactModel,3,isChecked);
                    }
                });
                dialog.show();
            break;
        }
    }
    void setChecked(ContactModel contactModel, int id, boolean isChecked){
        if (isChecked&&!contactModel.groupList.contains(id)) contactModel.groupList.add(id);
        else contactModel.groupList.remove(id);
        arrayListCurrent = getFilteredListContact();
        contactAdapter.arrayListDisplayed = arrayListCurrent;
        contactAdapter.getFilter().filter(editTextCopy.getText().toString());
    }
    void setInitialChecked(ContactModel contactModel, int id, CheckBox checkBox){
        if (contactModel.groupList.contains(id))checkBox.setChecked(true);
        else checkBox.setChecked(false);
    }
    protected void inicializeArray() {

        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name1),
                getResources().getString(R.string.street1),
                getResources().getString(R.string.tel1)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name2),
                getResources().getString(R.string.street2),
                getResources().getString(R.string.tel2)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name3),
                getResources().getString(R.string.street3),
                getResources().getString(R.string.tel3)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name4),
                getResources().getString(R.string.street4),
                getResources().getString(R.string.tel4)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name5),
                getResources().getString(R.string.street5),
                getResources().getString(R.string.tel5)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name6),
                getResources().getString(R.string.street6),
                getResources().getString(R.string.tel6)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name7),
                getResources().getString(R.string.street7),
                getResources().getString(R.string.tel7)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name8),
                getResources().getString(R.string.street8),
                getResources().getString(R.string.tel8)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name9),
                getResources().getString(R.string.street9),
                getResources().getString(R.string.tel9)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name10),
                getResources().getString(R.string.street10),
                getResources().getString(R.string.tel10)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name11),
                getResources().getString(R.string.street11),
                getResources().getString(R.string.tel11)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name12),
                getResources().getString(R.string.street12),
                getResources().getString(R.string.tel12)));
        arrayListContactAll.add(new ContactModel(getResources().getInteger(R.integer.idAllGroup),
                getResources().getString(R.string.name13),
                getResources().getString(R.string.street13),
                getResources().getString(R.string.tel13)));

        contactAdapter = new ContactAdapter(this,arrayListContactAll);
        listViewContact.setAdapter(contactAdapter);
    }

    public class ContactAdapter extends BaseAdapter implements Filterable{
        ArrayList<ContactModel> arrayListDisplayed;
        LayoutInflater inflater;

        public ContactAdapter(Context context, ArrayList<ContactModel> arrayListDisplayed) {
            this.arrayListDisplayed = arrayListDisplayed;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return arrayListDisplayed.size();
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        private class ViewHolder {
            TextView contactTv;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if (convertView ==null){
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.contact_textview,null);
                viewHolder.contactTv = (TextView) convertView.findViewById(R.id.idContactItem);
                convertView.setTag(viewHolder);
            }else{
                 viewHolder = (ViewHolder) convertView.getTag();
            }
                viewHolder.contactTv.setText(arrayListDisplayed.get(position).name);
                viewHolder.contactTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, arrayListDisplayed.get(position).name + "\n"
                                + arrayListDisplayed.get(position).address+ "\n"
                                + arrayListDisplayed.get(position).tel+ "\n"
                                + getResources().getInteger(R.integer.idAllGroup), Toast.LENGTH_SHORT).show();
                        createDialogs(R.id.idDialogContactInfo, arrayListDisplayed.get(position));
                    }
                });

            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<ContactModel> filteredArrList = new ArrayList<ContactModel>();


                    if (constraint == null || constraint.length() == 0) {
                        results.count = arrayListCurrent.size();
                        results.values = arrayListCurrent;
                    }else {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < arrayListCurrent.size(); i++) {
                            String data = arrayListCurrent.get(i).name;
                            if (data.toLowerCase().startsWith(constraint.toString())) {
                                filteredArrList.add(arrayListCurrent.get(i));
                            }
                        }
                        // set the Filtered result to return
                        results.count = filteredArrList.size();
                        results.values = filteredArrList;
                    }
                    return results;
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    arrayListDisplayed = (ArrayList<ContactModel>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }
    ArrayList<ContactModel> getFilteredListContact(){
        ArrayList<ContactModel> arrayList = null;
        switch (currentIdGroup){
            case 0:
                arrayList = arrayListContactAll;
                break;
            case 1:
                arrayList = containsContactGroup(1);
                break;
            case 2:
                arrayList = containsContactGroup(2);
                break;
            case 3:
                arrayList = containsContactGroup(3);
                break;
        }
        return arrayList;
    }
    ArrayList<ContactModel> containsContactGroup(int id){

        ArrayList<ContactModel> arrayList = new ArrayList<>();

        for (int i = 0; i < arrayListContactAll.size(); i++) {
            ContactModel contactModel = arrayListContactAll.get(i);
            if (contactModel.groupList.contains(id))arrayList.add(contactModel);
        }
        return arrayList;
    }
}
