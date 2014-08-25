module Api
  module V1
    class SystemInfosController < DevicesController
      include DatabaseConnection

      public

      private
        def system_info_params
          params.permit(
            :os_version,
            :model,
            :product,
            :manufacturer,
            :brand,
            :android_id,
            :user_name
          )
        end
    end
  end
end