class AppSession
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :app_sessions

  field :app_session_timestamp_start, type: String
  field :app_session_timestamp_end, type: String
  field :app_session_timestamp_duration, type: String
  field :server_sent, type: Integer
end
