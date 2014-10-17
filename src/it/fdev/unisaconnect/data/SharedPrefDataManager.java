package it.fdev.unisaconnect.data;

import it.fdev.encryptionUtils.CryptoMan_2;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.utils.MyFragmentInterface;
import it.fdev.utils.ObjectSerializer;
import it.fdev.utils.Utils;

import java.io.Serializable;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.securepreferences.SecurePreferences;

/**
 * Gestisce i dati salvati nelle sharedpreferences
 * Cifra username e password
 * @author francesco
 *
 */
public class SharedPrefDataManager {
	
	public static final String SSID_STUDENTI = "Studenti";
	
	private SharedPreferences mPrefs = null;
	private SecurePreferences mSecurePrefs = null;
	
	private static final String PREFERENCES_KEY = "PREFERENCES";
	
	// Boot Fragment
	private static final String PREF_BOOTABLE_FRAGMENT = "bootableFragment";
	// Login
	private static final String PREF_USER 			= "user";
	private static final String PREF_PASS 			= "pass";
	private static final String PREF_TIPO_CORSO		= "tipoCorso";
	private static final String PREF_AUTOLOGIN 		= "loginAutomatica";
	private static final String PREF_NOME_COGNOME 	= "nomeCognome";
	// Mensa
	private static final String PREF_MENU_MENSA = "menu";
	// Weather
	private static final String PREF_WEATHER = "weather";
	// Presenze
	private static final String PREF_PRESENZE = "presenze";
	// Appelli
	private static final String PREF_APPELLI = "appelli";
	// Libretto
	private static final String PREF_LIBRETTO_DATE = "libretto_date";
	private static final String PREF_LIBRETTO_SORT_BY_NAME = "libretto_sort_by_name";
	// Pagamenti
	private static final String PREF_PAGAMENTI = "pagamenti";
	// Biblioteca
	private static final String PREF_BIBLIO_LAST_SEARCH = "biblio_last_search";
	// Webmail
	private static final String PREF_MAIL_DO_CHECK = "webmail_do_check";
	private static final String PREF_MAIL_LAST_READ = "webmail_last_read";

	
	
	// Testing
	private static final String PREF_TESTING_ENABLED = "testingEnabled";
	// Crypto
	private static final String PREF_ENCRYPTION_VERSION = "encryptionVersion";
	private static final int CRYPTO_VERSION = 4;	// 2=0.5 3=0.6.2 4=0.6.9
	
	
	public SharedPrefDataManager(Context context) {
		mSecurePrefs = new SecurePreferences(context);
		mPrefs = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
		
		int cVersion = mPrefs.getInt(PREF_ENCRYPTION_VERSION, CRYPTO_VERSION);
		if (cVersion < CRYPTO_VERSION) {
			updateDataToCurrentVersion();
		}
		
		mPrefs.edit().putInt(PREF_ENCRYPTION_VERSION, CRYPTO_VERSION).commit();
	}
	
	public void updateDataToCurrentVersion() {
		try {
			if (CRYPTO_VERSION == 4) { //Intro to securepreferences
				
				if (mSecurePrefs.getString(PREF_USER, null)!=null && mSecurePrefs.getString(PREF_PASS, null)!=null) {
					return;
				}
				
				String username, password;
				String userCod = mPrefs.getString(PREF_USER, null);
				String passCod = mPrefs.getString(PREF_PASS, null);
				if (userCod != null && passCod != null) {
					username = CryptoMan_2.decrypt(userCod);
					password = CryptoMan_2.decrypt(passCod);
				} else {
					username = null;
					password = null;
					removeData();
				}
				if (username==null || password==null) {
					return;
				}
				
				Editor editor = mPrefs.edit();
				editor.remove(PREF_USER);
				editor.remove(PREF_PASS);
				editor.commit();
				
				com.securepreferences.SecurePreferences.Editor secureEditor = mSecurePrefs.edit();
				secureEditor.putString(PREF_USER, username);
				secureEditor.putString(PREF_PASS, password);
				secureEditor.commit();
			}
		} catch(Exception e) {
			Log.e(Utils.TAG, "Exception converting to secureEditor", e);
			removeData();
		}
	}
	
	//Controlla che siano stati precedentemente salvati i dati di login
	public boolean loginDataExists() {
		return (mSecurePrefs.contains(PREF_USER) && getUser()!=null &&
				mSecurePrefs.contains(PREF_PASS) && getPass()!=null &&
				mPrefs.contains(PREF_AUTOLOGIN));
	}
	
	private void removeData() {
		Log.d(Utils.TAG, "DataManager vacuum preferences");
		Editor editor = mPrefs.edit();
		editor.clear().commit();
	}
	
	public String getUser() {
		return mSecurePrefs.getString(PREF_USER, null);
	}

	public void setUser(String user) {
		user = (user.contains("@") ?  user.substring(0,user.lastIndexOf("@")) : user); //elimino la @ e seguito
		com.securepreferences.SecurePreferences.Editor secureEditor = mSecurePrefs.edit();
		secureEditor.putString(PREF_USER, user);
		secureEditor.commit();
	}

	public String getPass() {
		return mSecurePrefs.getString(PREF_PASS, null);
	}

	public void setPass(String pass) {
		com.securepreferences.SecurePreferences.Editor secureEditor = mSecurePrefs.edit();
		secureEditor.putString(PREF_PASS, pass);
		secureEditor.commit();
	}
	
	public int getTipoCorso() {
		return mPrefs.getInt(PREF_TIPO_CORSO, 1);
	}
	
	public void setTipoCorso(int tipoCorso) {
		saveField(PREF_TIPO_CORSO, tipoCorso);
	}

	public boolean isLoginAutomatica() {
		return mPrefs.getBoolean(PREF_AUTOLOGIN, true);
	}

	public void setLoginAutomatica(boolean loginAutomatica) {
		saveField(PREF_AUTOLOGIN, loginAutomatica);
	}

	public boolean isTestingingEnabled() {
		return mPrefs.getBoolean(PREF_TESTING_ENABLED, MainActivity.isTestingAPK);
	}
	
	public void setTestingEnabled(boolean testingEnabled) {
		saveField(PREF_TESTING_ENABLED, testingEnabled);
	}
	
	public String getNomeCognome() {
		return mPrefs.getString(PREF_NOME_COGNOME, null);
	}
	
	public void setNomeCognome(String nomeCognome) {
		saveField(PREF_NOME_COGNOME, nomeCognome);
	}
	
	public MenuMensa getMenuMensa() {
		return (MenuMensa) ObjectSerializer.deserialize(mPrefs.getString(PREF_MENU_MENSA, null));
	}
	
	public void setMenuMensa(MenuMensa menuMensa) {
		saveField(PREF_MENU_MENSA, menuMensa);
	}
	
	public WeatherData getWeather() {
		return (WeatherData) ObjectSerializer.deserialize(mPrefs.getString(PREF_WEATHER, null));
	}
	
	public void setWeather(WeatherData weather) {
		saveField(PREF_WEATHER, weather);
	}
	
	public Presenze getPresenze() {
		return (Presenze) ObjectSerializer.deserialize(mPrefs.getString(PREF_PRESENZE, null));
	}
	
	public void setPresenze(Presenze presenze) {
		saveField(PREF_PRESENZE, presenze);
	}
	
	public Date getLibrettoFetchDate() {
		return new Date(mPrefs.getLong(PREF_LIBRETTO_DATE, 0));
	}
	
	public void setLibrettoFetchDate(Date librettoFetchDate) {
		saveField(PREF_LIBRETTO_DATE, librettoFetchDate.getTime());
	}
	
	public boolean getLibrettoSortByName() {
		return mPrefs.getBoolean(PREF_LIBRETTO_SORT_BY_NAME, true);
	}
	
	public void setLibrettoSortByName(boolean librettoSortByName) {
		saveField(PREF_LIBRETTO_SORT_BY_NAME, librettoSortByName);
	}
	
	public Appelli getAppelli() {
		return (Appelli) ObjectSerializer.deserialize(mPrefs.getString(PREF_APPELLI, null));
	}
	
	public void setAppelli(Appelli appelli) {
		saveField(PREF_APPELLI, appelli);
	}
	
	public Pagamenti getPagamenti() {
		return (Pagamenti) ObjectSerializer.deserialize(mPrefs.getString(PREF_PAGAMENTI, null));
	}

	public void setPagamenti(Pagamenti pagamenti) {
		saveField(PREF_PAGAMENTI, pagamenti);
	}
	
	public String getBiblioLastSearch() {
		return mPrefs.getString(PREF_BIBLIO_LAST_SEARCH, "");
	}
	
	public void setBiblioLastSearch(String lastSearch) {
		saveField(PREF_BIBLIO_LAST_SEARCH, lastSearch);
	}
	
	public boolean getMailDoCheck() {
		return mPrefs.getBoolean(PREF_MAIL_DO_CHECK, true);
	}
	
	public void setMailDoCheck(boolean doCheck) {
		saveField(PREF_MAIL_DO_CHECK, doCheck);
	}
	
	public Date getMailLastRead() {
		return new Date(mPrefs.getLong(PREF_MAIL_LAST_READ, 0));
	}
	
	public void setMailLastRead(Date lastReadDate) {
		saveField(PREF_MAIL_LAST_READ, lastReadDate.getTime());
	}
	
	public Class<? extends MyFragmentInterface> getBootFragmentClass() {
		return MainActivity.fragmentsIDs.get(mPrefs.getInt(PREF_BOOTABLE_FRAGMENT, 0));
	}

	public void setBootFragmentClass(Class<? extends MyFragmentInterface> bootFragmentClass) {
//		editor.putInt(PREF_BOOTABLE_FRAGMENT, Math.max(MainActivity.BootableFragmentsEnum.indexOf(bootFragmentClass), 0));
//		saveField(PREF_BOOTABLE_FRAGMENT, Math.max(MainActivity.BootableFragmentsEnum.indexOf(bootFragmentClass), 0)));
	}
	
	private boolean saveField(String field, Object value) {
		Editor editor = mPrefs.edit();
		try {
			if (value instanceof Integer) {
				editor.putInt(field, (Integer) value);
			} else if (value instanceof Long) {
				editor.putLong(field, (Long) value);
			} else if (value instanceof String) {
				editor.putString(field, (String) value);
			} else if (value instanceof Boolean) {
				editor.putBoolean(field, (Boolean) value);
			} else {
				editor.putString(field, ObjectSerializer.serialize((Serializable) value));
			}
		} catch(Exception e) {
			Log.e(Utils.TAG, "Eccezione codificando i dati...resetto: " + field, e);
			editor.remove(field);
			return false;
		} finally {
			editor.commit();
		}
		return true;
	}

}
