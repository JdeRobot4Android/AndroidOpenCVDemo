package org.jderobot.androidopencvdemo;

import org.jderobot.androidopencvdemo.R;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class Preferences extends PreferenceActivity implements OnPreferenceChangeListener {

	EditTextPreference protocol;
	EditTextPreference ipaddress;
	EditTextPreference port;
	
  static boolean modified = false;
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preference);
      protocol = (EditTextPreference) findPreference("protocol");
      ipaddress = (EditTextPreference) findPreference("ipaddress");
      port = (EditTextPreference) findPreference("port");
      
      protocol.setOnPreferenceChangeListener(this);
      ipaddress.setOnPreferenceChangeListener(this);
      port.setOnPreferenceChangeListener(this);

  }

@Override
public boolean onPreferenceChange(Preference preference, Object newValue) {
	// TODO Auto-generated method stub
	if(preference.equals(protocol))
		protocol.setText(newValue.toString());
	if(preference.equals(ipaddress))
		ipaddress.setText(newValue.toString());
	if(preference.equals(port))
		port.setText(newValue.toString());
	modified = true;
	return false;
}
}


