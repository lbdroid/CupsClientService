package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.cupsprintservice.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView tv = (TextView) findViewById(R.id.abouttext);
		PackageInfo pInfo;
		String version = "";
		
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		String html = "<h1>CupsPrintService " + version + "</h1>";

		tv.setText(Html.fromHtml(html));
		tv.setMovementMethod(LinkMovementMethod.getInstance());	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
