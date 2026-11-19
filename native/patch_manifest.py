import sys
p = sys.argv[1]
s = open(p, encoding='utf-8').read()
perms = '''    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
'''
comp = '''        <service android:name=".FloatService" android:exported="false" android:foregroundServiceType="specialUse">
            <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="floating_editor_overlay" />
        </service>
        <activity android:name=".PickerActivity" android:exported="false" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
'''
assert '</application>' in s and '<application' in s
s = s.replace('</application>', comp + '    </application>', 1)
s = s.replace('<application', perms + '    <application', 1)
open(p, 'w', encoding='utf-8').write(s)
print('manifest ok')
