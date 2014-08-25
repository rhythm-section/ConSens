class AppUsage
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :app_usages

  field :timestamp_start, type: String
  field :timestamp_end, type: String
  field :timestamp_duration, type: String
  field :app_session_id, type: Integer
  field :app_name, type: String
  field :package_name, type: String
  field :version_name, type: String
  field :version_code, type: Integer
  field :base_activity, type: String
  field :top_activity, type: String
  field :first_install_time, type: String
  field :last_update_time, type: String
  field :server_sent, type: Integer
end
