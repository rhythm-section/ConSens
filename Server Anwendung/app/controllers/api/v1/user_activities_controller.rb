module Api
  module V1
    class UserActivitiesController < DevicesController
      include DatabaseConnection

      public

      private
        def user_activity_params
          # params.require(:user_activities).permit(:activity_app_session_id, :activity_name, :activity_confidence, :activity_type, :activity_timestamp)
          params.permit(
            :activity_app_session_id,
            :activity_name,
            :activity_confidence,
            :activity_type,
            :activity_timestamp,
            :server_sent
          )
        end

    end
  end
end