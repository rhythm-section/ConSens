module Api
  module V1
    class UserLocationsController < DevicesController
      include DatabaseConnection

      public

      private
        def user_location_params
          params.permit(
            :location_app_session_id,
            :location_timestamp,
            :location_elapsed_realtime_nanos,
            :location_provider,
            :location_latitude,
            :location_longitude,
            :location_altitude,
            :location_speed,
            :location_accuracy,
            :server_sent
          )
        end
    end
  end
end