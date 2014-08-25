class SystemSetting
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :system_settings

  field :settings_app_session_id, type: Integer
  field :settings_timestamp, type: String
  field :settings_airplane_mode, type: Integer
  field :settings_bluetooth, type: Integer
  field :settings_data_roaming, type: Integer
  field :settings_development_settings_enabled, type: Integer
  field :settings_http_proxy, type: String
  field :settings_mode_ringer, type: String
  field :settings_volume_alarm, type: String
  field :settings_volume_music, type: String
  field :settings_volume_notification, type: String
  field :settings_volume_ring, type: String
  field :settings_volume_system, type: String
  field :settings_volume_voice, type: String
  field :settings_network_preference, type: String
  field :settings_stay_on_while_plugged_in, type: String
  field :settings_usb_mass_storage_enabled, type: Integer
  field :settings_wifi, type: Integer
  field :settings_wifi_ssid, type: String
  field :settings_location_mode_3, type: String
  field :settings_location_mode_17, type: Integer
  field :server_sent, type: Integer
end
