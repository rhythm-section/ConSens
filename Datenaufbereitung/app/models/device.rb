class Device < ActiveRecord::Base
  has_many :locations
  has_many :location_clusters
  has_many :activities
  has_many :app_sessions
  has_many :app_usages
  has_many :system_settings

  # geocoded_by :start_address, latitude: :start_latitude, longitude: :start_longitude
end
