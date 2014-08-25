class SystemInfo
  include Mongoid::Document
  include Mongoid::Timestamps

  embedded_in :device, inverse_of: :system_infos

  field :os_version, type: String
  field :model, type: String
  field :product, type: String
  field :manufacturer, type: String
  field :brand, type: String
  field :android_id, type: String
  field :user_name, type: String
end
