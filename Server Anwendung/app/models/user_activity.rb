class UserActivity
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :user_activities

  field :activity_app_session_id, type: Integer
  field :activity_timestamp, type: String
  field :activity_name, type: String
  field :activity_type, type: Integer
  field :activity_confidence, type: Integer
  field :server_sent, type: Integer
end
