class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  helper_method :unnecessary_apps

  def unnecessary_apps
    unnecessary_apps = [
      'at.lukasmayerhofer.consens',
      'com.android.launcher',                             # launcher
      'com.android.systemui',
      'com.cyanogenmod.trebuchet',                        # launcher
      'com.google.android.googlequicksearchbox',
      'com.htc.launcher',                                 # launcher
      'com.nidhoeggr.stressdetector.main',
      'com.nidhoeggr.thestresscollector',
      'thesis.dumb.alert'
    ]
  end
end
