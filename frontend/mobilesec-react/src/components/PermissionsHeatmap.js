import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { Box, Typography, useTheme } from '@mui/material';
import { motion } from 'framer-motion';

const PermissionsHeatmap = ({ permissions }) => {
    const theme = useTheme();

    // Categorize permissions (Simple heuristic for demo purposes)
    const categories = {
        Dangerous: 0,
        Privacy: 0,
        Network: 0,
        Storage: 0,
        Other: 0,
    };

    permissions.forEach(perm => {
        const p = perm.toUpperCase();
        if (p.includes('DANGEROUS') || p.includes('INSTALL') || p.includes('DELETE')) categories.Dangerous++;
        else if (p.includes('CAMERA') || p.includes('LOCATION') || p.includes('CONTACTS') || p.includes('RECORD')) categories.Privacy++;
        else if (p.includes('INTERNET') || p.includes('WIFI') || p.includes('NETWORK')) categories.Network++;
        else if (p.includes('STORAGE') || p.includes('MEDIA')) categories.Storage++;
        else categories.Other++;
    });

    const data = Object.keys(categories).map(key => ({
        name: key,
        count: categories[key],
    })).filter(item => item.count > 0);

    return (
        <Box
            component={motion.div}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            sx={{
                width: '100%',
                height: 300,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                bgcolor: 'background.paper',
                borderRadius: 2,
                p: 2,
                boxShadow: theme.shadows[1]
            }}
        >
            <Typography variant="h6" gutterBottom fontWeight="600">
                Permission Categories
            </Typography>
            {data.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                        <XAxis type="number" hide />
                        <YAxis dataKey="name" type="category" width={80} tick={{ fontSize: 12 }} />
                        <Tooltip
                            cursor={{ fill: 'transparent' }}
                            contentStyle={{ borderRadius: 8, border: 'none', boxShadow: theme.shadows[3] }}
                        />
                        <Bar dataKey="count" radius={[0, 4, 4, 0]} barSize={20}>
                            {data.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={theme.palette.primary.main} />
                            ))}
                        </Bar>
                    </BarChart>
                </ResponsiveContainer>
            ) : (
                <Box display="flex" alignItems="center" justifyContent="center" height="100%">
                    <Typography variant="body2" color="text.secondary">
                        No permissions to visualize.
                    </Typography>
                </Box>
            )}
        </Box>
    );
};

export default PermissionsHeatmap;
