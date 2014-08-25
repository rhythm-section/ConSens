desc "Adding app categories."
task :add_categories => :environment do
  # market_bot
  def app_exists?(app_id)
    begin
      return !!MarketBot::Android::App.new(app_id).update.title
    rescue
      return false
    end
  end

  # def get_category(package_name)
  #   response = HTTParty.get("https://42matters.com/api/1/apps/lookup.json?access_token=10d02adee2e08c826ee658fb7fa68bf1108a9d54&p=#{package_name}&fields=category")
  #   if response.code == 404
  #     return false
  #   else
  #     return response.parsed_response["category"]
  #   end
  # end

  enhanced_app_categories = {}
  apps = AppUsage.all

  apps.each_with_index do |app, index|
    package_name = app.package_name

    if app.app_category.blank?
      # category = get_category(package_name)
      # unless !category
      #   puts "Add category #{category} to #{package_name}"
      #   apps.where(package_name: package_name).update_all(app_category: category)
      # end
      if app_exists?(package_name)
        app = MarketBot::Android::App.new(package_name).update
        category = MarketBot::Android::App.new(package_name).update.category
        
        if category != ''
          apps.where(package_name: package_name).update_all(app_category: category)
          puts "#{index}. Added #{category} to #{package_name}"
        else
          puts "#{index}. Error while retrieving category."
          next
        end
      end
    else
      puts "#{index}. Category already there."
    end
  end
end