class AppUsage < ActiveRecord::Base
  belongs_to :device
  belongs_to :app_session
end
