module Api
  module V1
    class AppSessionsController < DevicesController
      include DatabaseConnection

      public

      private
        def app_session_params
          params.permit(
            :app_session_timestamp_start,
            :app_session_timestamp_end,
            :app_session_timestamp_duration,
            :server_sent
          )
        end
    end
  end
end