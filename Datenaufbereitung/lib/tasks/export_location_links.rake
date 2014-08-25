desc "Export Location Links."
task :export_location_links => :environment do

  devices = Device.all

  devices.each do |device|
    # setting file path
    user_name = device.user_name.underscore.gsub(' ', '_')
    location_mail_path = Rails.root.join('data', 'export', 'location_mail', "#{user_name}.html")

    puts "Writing location links to: #{location_mail_path}"

    # add location count to each cluster
    clusters = device.location_clusters
    locations = device.locations

    clusters_with_location_count = []
    clusters.each do |cluster|
      new_cluster = Hash[cluster.attributes]
      count = locations.where(cluster: cluster.id).count

      if count > 0
        new_cluster['count'] = count
        clusters_with_location_count.push(new_cluster)
      end
    end

    # sort in descending order (location count)
    clusters_with_location_count.sort_by!{ |location| location['count'] }.reverse!

    #write location cluster links to file
    File.open(location_mail_path, 'w') do |f|
      f.puts "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>"
      f.puts "<html xmlns='http://www.w3.org/1999/xhtml'>"
      f.puts "<head>"
      f.puts "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />"
      f.puts "<title>HTML Email Template</title>"
      f.puts "<style type='text/css'>"
      f.puts "body { font-family: Helvetica; font-size: 12px; }"
      f.puts "</style>"
      f.puts "</head>"
      f.puts "<body>"
      f.puts "<table>"
      f.puts "<tr>"
      f.puts "<td>Adresse</td>"
      f.puts "<td>Kategorie</td>"
      f.puts "</tr>"
      clusters_with_location_count.each_with_index do |cluster, index|
        f.puts "<tr>"
        f.puts "<td>#{index}. <a href='http://maps.google.com/maps?z=10&q=#{cluster['latitude']},#{cluster['longitude']}&mrt=yp'>#{cluster['address']}</a> (#{cluster['count']}x aufgezeichnet)</td>"
        f.puts "<td></td>"
        f.puts "</tr>"
      end
      f.puts "</table>"
      f.puts "</body>"
      f.puts "</html>"
    end
  end
end