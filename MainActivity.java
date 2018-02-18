package com.ygoproject.nawaf.yugiohdeckbuilder.Activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ygoproject.nawaf.yugiohdeckbuilder.CardSearchFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.DeckManagerFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.DeckSpinnerAdapter;
import com.ygoproject.nawaf.yugiohdeckbuilder.FavoriteFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.PagerCardMakerFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.PagerSearchFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.R;
import com.ygoproject.nawaf.yugiohdeckbuilder.SetsSearchFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.SettingsFragment;
import com.ygoproject.nawaf.yugiohdeckbuilder.TrendingPager;
import com.ygoproject.nawaf.yugiohdeckbuilder.Utils;
import com.ygoproject.nawaf.yugiohdeckbuilder.database.DbUpdateTask;
import com.ygoproject.nawaf.yugiohdeckbuilder.database.YugiContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
    , LoaderManager.LoaderCallbacks<Void>
{

    /** TODO: to Complete the Project:
     * - Trend ( 1. change remove holder , 2. cache the fragment? )
     */

    private  NavigationView navigation_View;
    private DrawerLayout mDrawerLayout;
    private Menu menu;
    private  App app;

    @Override
    protected void onStart() {
        Log.d("mainActivity","onStart()");
        super.onStart();
        getLoaderManager().restartLoader(1,null,this).forceLoad();
    }

    protected final void onCreate(Bundle savedInstanceState)
    {
        Log.d("mainActivity","onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar_NavigationView();
        setup_deckSpinner();
        CardSearchFragment listFragment = new CardSearchFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,listFragment).commit();

            // ======== add Fav deck ========
        if(!Utils.SHP.getInitDB(this)) { // if not init DB
            ContentValues coValues = new ContentValues();
            coValues.put
                    (YugiContract.DeckEntry.COULMN_NAME, "Favorite");
            getContentResolver().insert(
                    YugiContract.DeckEntry.CONTENT_URI, coValues);
            Utils.SHP.setInitDB(this);
        }

        // ======== Check Database + App updates ========
        checkUpdates();

        handleAdmob();

    }


    private void handleAdmob() {
        app = (App) getApplication();
//        app.loadBannerAd((ConstraintLayout) findViewById(R.id.constrainLay_mainActivity));
    }

    @Override
    protected void onResume() {
        app.loadBannerAd((ConstraintLayout) findViewById(R.id.constrainLay_mainActivity));
        findViewById(R.id.fragment_container).
                setPadding(0,0,0,App.getBannerHeight(getBaseContext()));
        super.onResume();
    }

    private void setupToolbar_NavigationView(){
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        navigation_View = findViewById(R.id.nav_view);
        navigation_View.setItemIconTintList(null);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(this,mDrawerLayout,myToolbar,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigation_View.setNavigationItemSelectedListener(this);
        navigation_View.setCheckedItem(R.id.nav_cards);

    }

    private void setup_deckSpinner(){
        Spinner spinner = navigation_View.getHeaderView(0).findViewById(R.id.deck_spinner);
        DeckSpinnerAdapter adapter = new DeckSpinnerAdapter(this);
        spinner.setOnItemSelectedListener(adapter);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCurrentDeckIndex());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.isChecked()) {  mDrawerLayout.closeDrawers(); return true; }
        navigation_View.setCheckedItem(item.getItemId());

        // Dialog ads ====================
        app.loadDialogAd();
        // ==================================


        switch (item.getItemId()) {
            case R.id.nav_cards:
                mDrawerLayout.closeDrawers();
                CardSearchFragment listFragment = new CardSearchFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
                break;
            case R.id.nav_deck:
                mDrawerLayout.closeDrawers();
                    DeckManagerFragment deckFragment = new DeckManagerFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, deckFragment).commit();
                    break;

                    case R.id.nav_settings:
                        mDrawerLayout.closeDrawers();
                        SettingsFragment settingsFragment = new SettingsFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
                        break;
            case R.id.nav_favorite :
                mDrawerLayout.closeDrawers();
                FavoriteFragment favoriteFragment = new FavoriteFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, favoriteFragment).commit();
                break;

            case R.id.nav_advanceSearch :
                mDrawerLayout.closeDrawers();
                PagerSearchFragment searchFragment = new PagerSearchFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, searchFragment).commit();
                break;

            case R.id.nav_card_maker :
                mDrawerLayout.closeDrawers();
                PagerCardMakerFragment cardMakerFragment = new PagerCardMakerFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, cardMakerFragment).commit();
                break;

            case R.id.nav_booster :
                mDrawerLayout.closeDrawers();
                SetsSearchFragment setsSearchFragment = new SetsSearchFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, setsSearchFragment).commit();
                break;
            case R.id.nav_trending:
                // TODO TEMP
                    mDrawerLayout.closeDrawers();
                    TrendingPager trendingFragment = new TrendingPager();
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fragment_container, trendingFragment).commit();
                    break;
        }
        return false;
    }

    public void hideSearchView(){
        if (menu != null)
        menu.findItem(R.id.searchView).setVisible(false);
    }

    public void showSearchView(){
        if (menu != null)
            menu.findItem(R.id.searchView).setVisible(true);

    }

    public void hideChangeView(){
        if (menu != null)
            menu.findItem(R.id.change_view).setVisible(false);
    }

    public void showChangeView(){
        if (menu != null)
            menu.findItem(R.id.change_view).setVisible(true);

    }

    public void showDeckOption(){
        if (menu != null) {
            menu.findItem(R.id.clearCards).setVisible(true);
            menu.findItem(R.id.deleteDeck).setVisible(true);
            menu.findItem(R.id.renameDeck).setVisible(true);
            menu.findItem(R.id.addCard).setVisible(true);

        }

    }

    public void hideDeckOption(){
        if (menu != null) {
            menu.findItem(R.id.clearCards).setVisible(false);
            menu.findItem(R.id.deleteDeck).setVisible(false);
            menu.findItem(R.id.renameDeck).setVisible(false);
            menu.findItem(R.id.addCard).setVisible(false);
        }

    }



    public NavigationView getNavigation_View(){return navigation_View;}

    public boolean isSpinnerEmpty(){
        return getSpinner()
                .getAdapter().getCount() == 1;
    }

    public Spinner getSpinner(){
        return ( (Spinner) navigation_View.getHeaderView(0).findViewById(R.id.deck_spinner) );
    }

    public DeckSpinnerAdapter getSpinnerAdapter(){
        return ( (DeckSpinnerAdapter) getSpinner().getAdapter() );

    }


//    private int getFrameRes(int cardType_id){
//
//        // return frame res id from card
//        // 3,4,5,6,7,8,9,tt11,12,13,14,15,16,17,18,19,20,22,23,28,29
//        switch (cardType_id){
//            case 4 :case tt11 :case 14:case 15:case 16:case 18:case 22:case 23:case 28: //Effect : 3413 card's
//                return R.drawable.card_effect;
//            case 1 : return R.drawable.card_spell; // 1473 card's
//            case 2 : return R.drawable.card_trap; // 1232
//            case 3 :case 8: return R.drawable.card_normal; //normal : 772
//            case 7 :case 20: return R.drawable.card_xyz; // 341
//            case 5 :case 13 : return R.drawable.card_fusion; //264
//            case 9:case 17:case 19: return R.drawable.card_sycnhro; // 257
//            case 21:case 24:case 26: return R.drawable.card_pendulum_effect; // 170
//            case 6 :case 12:case 29: return R.drawable.card_ritual; // 54
//            case 10: return R.drawable.card_pendulum_normal; // 34
//            case 25 : return R.drawable.card_pendulum_xyz;    // 2
//            case 27 : return R.drawable.card_pendulum_synchro; // 2
//            case 30 : return R.drawable.card_pendulum_fusion; // art
//            default: return -1;
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.start_menu,menu);
        SearchView sv = (SearchView) menu.findItem(R.id.searchView).getActionView();
        int svColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            svColor = getResources().getColor(R.color.searchView_editText_color,null);
        else
            svColor = getResources().getColor(R.color.searchView_editText_color);

        ( (EditText)sv.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(svColor);
        sv.setQueryHint("Card (Name / Text)");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    ((ChangeViewInterface) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                            .filterList(newText);
                    return false;
                }catch (ClassCastException e){
                    return false;
                }
            }
        });
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.searchView :
                return true;

            case R.id.change_view :
                ( (ChangeViewInterface) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                        .changeViews();
                boolean flag = Utils.SHP.getIsGrid(getBaseContext());
                if(flag)
                    menu.findItem(R.id.change_view).setIcon(R.drawable.list);
                else
                    menu.findItem(R.id.change_view).setIcon(R.drawable.grid);
                return true;

            case R.id.clearCards :
                DeckManagerFragment deckFragment =
                        ( (DeckManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                if(! checkDeckAndShowMsg(deckFragment))
                        return true;
                new DeckSpinnerAdapter.ClearDeckDialog(this,deckFragment,Utils.SHP.getCurrentDeckID(this)).show();
                return true;

            case R.id.deleteDeck :
                DeckManagerFragment deckFragment2 =
                        ( (DeckManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                if(! checkDeckAndShowMsg(deckFragment2))
                    return true;
                new DeckSpinnerAdapter.DeleteDeckDialog(this,deckFragment2
                        ,Utils.SHP.getCurrentDeckID(this)).show();
                return true;

            case R.id.renameDeck :
                DeckManagerFragment deckFragment3 =
                        ( (DeckManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                if(! checkDeckAndShowMsg(deckFragment3))
                    return true;
                DeckSpinnerAdapter.AddDeckDialog dialog = new DeckSpinnerAdapter.AddDeckDialog(this,getSpinnerAdapter());
                dialog.setDeckFragment_client(deckFragment3);
                dialog.setRenameDeckDialog(true , deckFragment3.getDeck().getDeckID());
                dialog.show();
                return true;
            case R.id.addCard:
                if(isSpinnerEmpty()){
                    DeckManagerFragment deckFragment4 =
                            ( (DeckManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                    DeckSpinnerAdapter.AddDeckDialog dialog2 =
                            new DeckSpinnerAdapter.AddDeckDialog(this, getSpinnerAdapter());
                    dialog2.setDeckFragment_client(deckFragment4);
                    dialog2.show();
                    Toast.makeText(this, "There is no deck! , Please create one.", Toast.LENGTH_LONG).show();
                   getSpinnerAdapter().updateDecks();
                    return true;
                }
                Intent intent = new Intent(this, AddCardActivity.class);
                startActivity(intent);
                return true;
            default: return super.onOptionsItemSelected(item);


        }
    }

    private boolean checkDeckAndShowMsg(DeckManagerFragment fragment){
        // This method used in menuLogic in switch in decks options items
        if(fragment.getDeck() == null) {
            Toast.makeText(getBaseContext(),"Create Deck first",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof PagerCardMakerFragment)
            ( (PagerCardMakerFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container))
              .makerResultArtTask(requestCode , resultCode , data);

    }

    private void checkUpdates() {

        if(!Utils.isInternetConnected(this)) // IF No Internet
            return;

        new AppUpdateCheckTask(MainActivity.this).execute(); // Will check database also


    }

    public void updateCardSearchList(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if( fragment instanceof CardSearchFragment ){
            ((CardSearchFragment)fragment).updateCardList();
        }
    }

    @Override
    public Loader<Void> onCreateLoader(int i, Bundle bundle) {
        return new DeckLoader(MainActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void aVoid) {
        getSpinnerAdapter().updateDeckThread_ui();
        getSpinner().setSelection(getSpinnerAdapter().getCurrentDeckIndex());
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase,"en"));

    }

    static class DeckLoader extends AsyncTaskLoader<Void> {
        private WeakReference<MainActivity> mainActivity;

         DeckLoader(MainActivity mainActivity) {
            super(mainActivity.getBaseContext());
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public Void loadInBackground() {
            if(mainActivity.get() != null)
                mainActivity.get().getSpinnerAdapter().updateDeckThread_task();
            return null;
        }
    }
 

}


