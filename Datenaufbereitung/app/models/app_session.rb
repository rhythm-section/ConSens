class AppSession < ActiveRecord::Base
  belongs_to :device
  has_many :app_usages, :dependent => :destroy
end
