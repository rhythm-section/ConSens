# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20140821115453) do

  create_table "activities", force: true do |t|
    t.datetime "timestamp"
    t.string   "name"
    t.integer  "activity_type"
    t.integer  "confidence"
    t.integer  "app_session_id"
    t.integer  "device_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "app_sessions", force: true do |t|
    t.datetime "timestamp_start"
    t.datetime "timestamp_end"
    t.datetime "timestamp_duration"
    t.integer  "device_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "app_usages", force: true do |t|
    t.string   "app_name"
    t.string   "package_name"
    t.string   "version_name"
    t.integer  "version_code"
    t.string   "base_activity"
    t.string   "top_activity"
    t.datetime "first_install_time"
    t.datetime "last_update_time"
    t.integer  "app_session_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "device_id"
    t.string   "app_category"
  end

  create_table "devices", force: true do |t|
    t.string   "user_name"
    t.string   "mongo_id"
    t.string   "uuid"
    t.string   "android_id"
    t.string   "os_version"
    t.string   "model"
    t.string   "product"
    t.string   "manufacturer"
    t.string   "brand"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "location_clusters", force: true do |t|
    t.float    "latitude"
    t.float    "longitude"
    t.integer  "device_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "address"
    t.string   "label"
  end

  create_table "locations", force: true do |t|
    t.datetime "timestamp"
    t.integer  "elapsed_realtime_nanos"
    t.string   "provider"
    t.float    "latitude"
    t.float    "longitude"
    t.float    "altitude"
    t.float    "speed"
    t.float    "accuracy"
    t.integer  "app_session_id"
    t.integer  "device_id"
    t.integer  "cluster"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "system_settings", force: true do |t|
    t.datetime "timestamp"
    t.integer  "airplane_mode"
    t.integer  "bluetooth"
    t.integer  "data_roaming"
    t.integer  "development_settings_enabled"
    t.string   "http_proxy"
    t.string   "mode_ringer"
    t.string   "volume_alarm"
    t.string   "volume_music"
    t.string   "volume_notification"
    t.string   "volume_ring"
    t.string   "volume_system"
    t.string   "volume_voice"
    t.string   "network_preference"
    t.string   "stay_on_while_plugged_in"
    t.integer  "usb_mass_storage_enabled"
    t.integer  "wifi"
    t.string   "wifi_ssid"
    t.string   "location_mode_3"
    t.integer  "location_mode_17"
    t.integer  "app_session_id"
    t.integer  "device_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
