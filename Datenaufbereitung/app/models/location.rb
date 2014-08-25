class Location < ActiveRecord::Base
  belongs_to :device

  geocoded_by :latitude => :latitude, :longitude => :longitude
  after_validation :geocode
end
