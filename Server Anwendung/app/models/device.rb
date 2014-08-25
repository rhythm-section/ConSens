class Device
  include Mongoid::Document
  include Mongoid::Timestamps

  embeds_one  :system_infos, cascade_callbacks: true
  embeds_many :app_sessions, cascade_callbacks: true
  embeds_many :app_usages, cascade_callbacks: true
  embeds_many :system_settings, cascade_callbacks: true
  embeds_many :user_activities, cascade_callbacks: true
  embeds_many :user_locations, cascade_callbacks: true

  # accepts_nested_attributes_for :system_infos

  field :uuid, type: String
  # field :os_version, type: String
  # field :device, type: String
  # field :model, type: String
  # field :product, type: String
  # field :manufacturer, type: String
  # field :brand, type: String
  # field :android_id, type: String
end