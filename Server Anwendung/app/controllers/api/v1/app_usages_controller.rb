module Api
  module V1
    class AppUsagesController < DevicesController
      include DatabaseConnection

      public

      private
        def app_usage_params
          params.permit(
            :timestamp_start,
            :timestamp_end,
            :timestamp_duration,
            :app_session_id,
            :app_name,
            :package_name,
            :version_name,
            :version_code,
            :base_activity,
            :top_activity,
            :first_install_time,
            :last_update_time,
            :server_sent
          )
        end
    end
  end
end