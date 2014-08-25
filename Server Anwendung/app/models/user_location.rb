class UserLocation
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :user_locations

  field :location_app_session_id, type: Integer
  field :location_timestamp, type: String
  field :location_elapsed_realtime_nanos, type: Integer
  field :location_provider, type: String
  field :location_latitude, type: Float
  field :location_longitude, type: Float
  field :location_altitude, type: Float
  field :location_speed, type: Float
  field :location_accuracy, type: Float
  field :server_sent, type: Integer
end
